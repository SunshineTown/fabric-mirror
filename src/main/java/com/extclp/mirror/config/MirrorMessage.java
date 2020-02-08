package com.extclp.mirror.config;

import com.google.gson.annotations.SerializedName;

public class MirrorMessage{

    //common
    @SerializedName("common.diskFull")
    public String diskFull = "硬盘已满请清理硬盘";

    @SerializedName("common.mirrorIsLocked")
    public String mirrorIsLocked = "镜像 %s §r已被锁定";

    @SerializedName("common.unknownMirror")
    public String unknownMirror = "未知镜像 %s";

    @SerializedName("common.mirrorNotFound")
    public String mirrorNotFound = "没找到镜像 %s";

    @SerializedName("common.otherTaskRunning")
    public String otherTaskRunning = "有其他任务在进行";

    // reload
    @SerializedName("reload.reloadMessage")
    public String reloadMessage = "§2重载语言完成";

    @SerializedName("reload.reloadConfig")
    public String reloadConfig = "§2重载配置完成";

    //list
    @SerializedName("list.listMirrorHeader")
    public String listMirrorHeader = "§6------------镜像列表-------------";

    @SerializedName("list.listMirrorReturnButton")
    public ButtonText listMirrorReturnButton = new ButtonText("§a[回档]", "§a点击回档 §e%s");

    @SerializedName("list.listMirrorDeleteButton")
    public ButtonText listMirrorDeleteButton = new ButtonText("§4[删除]", "§c点击删除 §e%s");

    @SerializedName("list.listMirror")
    public String listMirror = "%s %s %s";

    @SerializedName("list.listMirrorCreatedTime")
    public String listMirrorCreatedTime = "创建时间: §3%s";

    @SerializedName("list.listMirrorDescription")
    public String listMirrorDescription = "§f镜像介绍: §3%s";

    @SerializedName("list.listMirrorOwner")
    public String listMirrorOwner = "§f创建者: §3%s";

    @SerializedName("list.listMirrorLock")
    public String listMirrorLock = "§c该镜像已经被锁定,你无法对该镜像进行修改";

    @SerializedName("list.listMirrorFooter")
    public String listMirrorFooter = "§6------------ total %s§6 -------------";

    //create
    @SerializedName("create.createMirrorFailurePrefix")
    public String createMirrorFailurePrefix = "创建镜像失败: ";

    @SerializedName("create.crateMirrorOverwriteButton")
    public ButtonText crateMirrorOverwriteButton = new ButtonText("§a[确定]", "§2点击确定");

    @SerializedName("create.crateMirrorOverwrite")
    public String crateMirrorOverwrite = "确定要覆盖 %s §r的备份么 %s";

    @SerializedName("create.createMirrorLimit")
    public String createMirrorLimit = "超过创建数量的最大限制 (%s/%s)";

    @SerializedName("create.startBackup")
    public String startBackup = "开始创建 %s §f备份";

    @SerializedName("create.backupFinish")
    public String backupFinish = "§a备份完成可以继续折腾了 用时: %s ms";

    @SerializedName("create.backupFailure")
    public String backupFailure = "§c好像备份失败了";

    //lock
    @SerializedName("lock.lockMirrorFailurePrefix")
    public String lockMirrorFailurePrefix = "锁定镜像失败: ";

    @SerializedName("lock.lockMirror")
    public String lockMirror = "已锁定镜像 %s";

    //unlock
    @SerializedName("unlock.unlockMirrorFailurePrefix")
    public String unlockMirrorFailurePrefix = "取消锁定镜像失败: ";

    @SerializedName("unlock.unlockMirror")
    public String unlockMirror = "已取消锁定镜像 %s";

    //return
    @SerializedName("return.returnMirrorFailurePrefix")
    public String returnMirrorFailurePrefix = "回档失败: ";

    @SerializedName("return.returnConfirm")
    public String returnConfirm = "你确定要回档到 %s §r的备份么 %s";

    @SerializedName("return.returnConfirmButton")
    public ButtonText returnConfirmButton = new ButtonText("§a[确定]", "§a点击确定");

    @SerializedName("return.unzipMirror")
    public String unzipMirror = "§2正在解压镜像 §f%s §2中请稍等";

    @SerializedName("return.unzipMirrorFailure")
    public String unzipMirrorFailure  = "无法解压镜像文件 %s";

    @SerializedName("return.serverWillRestart")
    public String serverWillRestart = "§2解压成功, 服务器即将回档到 %s§2 服务器即将重启";

    @SerializedName("return.cancelReturnMirrorButton")
    public ButtonText cancelReturnMirrorButton = new ButtonText("§4[取消]", "§4点击取消回档任务");

    @SerializedName("return.serverRestatingCountdown")
    public String serverRestatingCountdown = "§e服务器即将重启 §f%s %s";

    //cancel
    @SerializedName("cancel.cancelReturnMirror")
    public String cancelReturnMirror = "§2已取消回档任务";

    @SerializedName("cancel.noneReturnMirrorTask")
    public String noneReturnMirrorTask =  "取消回档失败: §c没有找到到回档任务";

    //delete
    @SerializedName("delete.deleteMirrorFailurePrefix")
    public String deleteMirrorFailurePrefix = "删除镜像失败: ";

    @SerializedName("delete.deleteMirrorConfirmButton")
    public ButtonText deleteMirrorConfirmButton = new ButtonText("§4[确定]", "§4点击确定");

    @SerializedName("delete.deleteMirrorConfirm")
    public String deleteMirrorConfirm = "确定要删除 %s §r的备份么? %s";

    @SerializedName("delete.deleteMirror")
    public String deleteMirror = "§4成功删除备份 %s";
}
