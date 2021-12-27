/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.ValidEnum;
import com.huawei.common.util.PasswordUtil;
import com.huawei.emergency.entity.EmergencyServer;
import com.huawei.emergency.entity.EmergencyServerExample;
import com.huawei.emergency.mapper.EmergencyServerMapper;
import com.huawei.emergency.service.EmergencyServerService;

import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.executor.RemoteScriptExecutor;
import com.huawei.script.exec.session.ServerInfo;
import com.huawei.script.exec.session.ServerSessionFactory;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 服务器信息管理
 *
 * @author y30010171
 * @since 2021-11-29
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class EmergencyServerServiceImpl implements EmergencyServerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyServerServiceImpl.class);

    @Value("${agent.uploadPath}")
    private String uploadPath;

    @Value("${agent.name}")
    private String agentName;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private EmergencyServerMapper serverMapper;

    @Autowired
    private RemoteScriptExecutor remoteExecutor;

    @Autowired
    private ServerSessionFactory sessionFactory;

    @Override
    public CommonResult<EmergencyServer> add(EmergencyServer server) {
        if (StringUtils.isEmpty(server.getServerIp()) || StringUtils.isEmpty(server.getServerName())) {
            return CommonResult.failed("请填写ip地址和名称");
        }

        EmergencyServerExample isServerNameExist = new EmergencyServerExample();
        isServerNameExist.createCriteria()
            .andServerNameEqualTo(server.getServerName())
            .andIsValidEqualTo(ValidEnum.VALID.getValue());
        isServerNameExist.or()
            .andServerIpEqualTo(server.getServerIp())
            .andIsValidEqualTo(ValidEnum.VALID.getValue());
        if (serverMapper.countByExample(isServerNameExist) > 0) {
            return CommonResult.failed("已存在服务器名称或服务器ip相同的主机");
        }

        CommonResult<EmergencyServer> serverResult = generateServer(server);
        if (StringUtils.isNotEmpty(serverResult.getMsg())) {
            return serverResult;
        }
        EmergencyServer insertServer = serverResult.getData();
        insertServer.setCreateUser(server.getCreateUser());
        insertServer.setCreateTime(new Date());
        insertServer.setUpdateUser(server.getCreateUser());
        insertServer.setUpdateTime(insertServer.getCreateTime());
        serverMapper.insertSelective(insertServer);
        insertServer.setPassword(null);
        return CommonResult.success(insertServer);
    }

    @Override
    public CommonResult delete(EmergencyServer server) {
        if (server.getServerId() == null) {
            return CommonResult.failed("请选择正确的主机信息");
        }
        EmergencyServer updateServer = new EmergencyServer();
        updateServer.setServerId(server.getServerId());
        updateServer.setIsValid(ValidEnum.IN_VALID.getValue());
        updateServer.setUpdateUser(server.getUpdateUser());
        updateServer.setUpdateTime(new Date());
        serverMapper.updateByPrimaryKeySelective(updateServer);
        return CommonResult.success();
    }

    @Override
    public CommonResult update(EmergencyServer server) {
        if (server.getServerId() == null) {
            return CommonResult.failed("请选择服务器");
        }
        CommonResult<EmergencyServer> serverResult = generateServer(server);
        if (StringUtils.isNotEmpty(serverResult.getMsg())) {
            return serverResult;
        }
        EmergencyServer updateServer = serverResult.getData();
        updateServer.setUpdateUser(server.getUpdateUser());
        updateServer.setUpdateTime(new Date());
        serverMapper.updateByPrimaryKeySelective(updateServer);
        return CommonResult.success();
    }

    @Override
    public CommonResult allServerUser(String serverIp) {
        return CommonResult.success(new String[]{"root", "guest"});
    }

    @Override
    public CommonResult queryServerInfo(CommonPage<EmergencyServer> params, String keyword, int[] excludeServerIds) {
        Page<EmergencyServer> pageInfo = PageHelper
            .startPage(params.getPageIndex(), params.getPageSize(), StringUtils.isEmpty(params.getSortType()) ? "" : params.getSortField() + System.lineSeparator() + params.getSortType())
            .doSelectPage(() -> {
                serverMapper.selectByKeyword(params.getObject(), keyword, excludeServerIds);
            });
        List<EmergencyServer> result = pageInfo.getResult();
        return CommonResult.success(result, (int) pageInfo.getTotal());
    }

    @Override
    public CommonResult search(String serverName) {
        EmergencyServer server = new EmergencyServer();
        server.setServerName(serverName);
        Object[] objects = serverMapper.selectByKeyword(server, null, null).stream().map(EmergencyServer::getServerName).toArray();
        return CommonResult.success(objects, objects.length);
    }

    @Override
    public CommonResult license(EmergencyServer server) {
        if (server.getServerId() == null || StringUtils.isEmpty(server.getLicensed())) {
            return CommonResult.failed("请选择主机和许可类型");
        }

        EmergencyServerExample isAgentOnline = new EmergencyServerExample();
        isAgentOnline.createCriteria()
            .andIsValidEqualTo(ValidEnum.VALID.getValue())
            .andAgentPortIsNotNull()
            .andServerIdEqualTo(server.getServerId());
        if (serverMapper.countByExample(isAgentOnline) == 0) {
            return CommonResult.failed("机器尚未安装agent,无需操作许可");
        }

        EmergencyServer updateServer = new EmergencyServer();
        updateServer.setServerId(server.getServerId());
        updateServer.setLicensed(!Boolean.parseBoolean(server.getLicensed()) ? "1" : "0");
        serverMapper.updateByPrimaryKeySelective(updateServer);
        return CommonResult.success();
    }

    @Override
    public CommonResult deleteServerList(String[] serverIds, String userName) {
        try {
            List<Integer> serverIdList = Arrays.stream(serverIds).map(Integer::parseInt).collect(Collectors.toList());
            EmergencyServerExample updateCondition = new EmergencyServerExample();
            updateCondition.createCriteria()
                .andServerIdIn(serverIdList);
            EmergencyServer updateServer = new EmergencyServer();
            updateServer.setIsValid(ValidEnum.IN_VALID.getValue());
            updateServer.setUpdateUser(userName);
            updateServer.setUpdateTime(new Date());
            serverMapper.updateByExampleSelective(updateServer, updateCondition);
        } catch (NumberFormatException e) {
            LOGGER.error("cast string to serverId error.", e);
            return CommonResult.failed("请选择正确的主机信息");
        }
        return CommonResult.success();
    }

    @Override
    public CommonResult install(EmergencyServer server) {
        if (server.getServerId() == null) {
            return CommonResult.failed("请选择主机");
        }
        EmergencyServer remoteServer = serverMapper.selectByPrimaryKey(server.getServerId());
        if (remoteServer == null) {
            return CommonResult.failed("请选择正确的主机");
        }
        ServerInfo serverInfo = new ServerInfo(remoteServer.getServerIp(), remoteServer.getServerUser());
        if ("1".equals(server.getHavePassword())) {
            try {
                serverInfo.setServerPassword(parsePassword(server));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Failed to parse password, {}", e.getMessage());
                return CommonResult.failed("获取服务器信息失败");
            }
        }
        Session session = null;
        try {
            session = sessionFactory.getSession(serverInfo);
            File agentFile = new File(agentName);
            ExecResult execResult = remoteExecutor.uploadFile(session, uploadPath, agentFile);
            if (!execResult.isSuccess()){
                return CommonResult.failed("上传agent失败");
            }
            execResult = remoteExecutor.exec(session,
                String.format(Locale.ROOT, "nohup java -jar %s%s%s >%s.log &", uploadPath, System.lineSeparator(), agentFile.getName(), agentFile.getName()),
                null, -1);
            if (!execResult.isSuccess()) {
                return CommonResult.failed("启动agent失败");
            }
        } catch (JSchException e) {
            LOGGER.error("Failed to connect  ip={}, {}", serverInfo.getServerIp(), e.getMessage());
            return CommonResult.failed("连接服务器失败");
        } catch (SftpException | IOException e) {
            LOGGER.error("Failed to operate ip={}, {}", serverInfo.getServerIp(), e.getMessage());
            return CommonResult.failed("操作远程服务器失败");
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
        return CommonResult.success();
    }

    public CommonResult<EmergencyServer> generateServer(EmergencyServer source) {
        EmergencyServer newServer = new EmergencyServer();
        newServer.setServerUser(source.getServerUser());
        newServer.setServerIp(source.getServerIp());
        newServer.setServerPort(source.getServerPort());
        newServer.setHavePassword(source.getHavePassword());
        newServer.setPasswordMode(source.getPasswordMode());
        newServer.setServerName(source.getServerName());
        if ("1".equals(source.getHavePassword())) {
            try {
                newServer.setPassword(passwordUtil.encodePassword(
                    "0".equals(source.getPasswordMode())
                        ? source.getPassword()
                        : getPassword(newServer.getServerIp(), newServer.getServerUser())
                ));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("encode password error.", e);
                return CommonResult.failed("密码加密失败");
            }
        }
        if ("1".equals(source.getPasswordMode())) {
            newServer.setServerUser(source.getPasswordUri());
            newServer.setPasswordUri(source.getPasswordUri());
        }
        return CommonResult.success(newServer);
    }

    public String getPassword(String serverIp, String serverUser) {
        return "123456";
    }

    public String parsePassword(EmergencyServer server) throws UnsupportedEncodingException {
        return passwordUtil.encodePassword(
            "0".equals(server.getPasswordMode())
                ? server.getPassword()
                : getPassword(server.getServerIp(), server.getServerUser()));
    }
}