package com.kedacom.touchdata.whiteboard.dialog;


import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;

public interface IDialogBtnEvent {
	/**
	 * 对话框取消按钮 事件
	 * @param dtype 对话框类型
	 */
	void onDCancelBtnEvent(BaseActivity.DialogType dtype);
	
	/**
	 * 兑换过框确认按钮事件
	 * @param dtype 对话框类型
	 */
	void onDSureBtnEvent(BaseActivity.DialogType dtype);
	
	/**
	 * 文件保存成功后提示对话框 “打开文件夹”按钮事件
	 * @param filePath 文件保存目录
	 */
	void onDOpenDirBtnEvent(String filePath);
	
	/**
	 * 文件已存在对话框  "重命名"按钮事件
	 */
	void onDResetFileNameBtnEvent();
	
	/**
	 * 文件已存在对话框 "替换"按钮事件
	 */
	void onDReplaceFileBtnEvent();

	/**
	 * 提示保存对话框 "全部保存"按钮事件
	 */
	void onDExitSaveBtnEvent();
	
	/**
	 * 提示保存对话框 "逐个保存"按钮事件
	 */
	void onDExitNotSaveBtnEvent();
	
	/**
	 * 提示保存对话框 "退出"按钮事件
	 */
	void onDExitCancelBtnEvent();

	/**
	 * 画笔尺寸更改
	 * @param size  更改后的画笔尺寸
      */
	void onDPenSizeBtnEvent(int size);

	/**
	 * 画笔颜色更改
	 * @param color 更改后的画笔颜色
     */
	void onDPenColorBtnEvent(int color);

    /**
	 * 当前选择普通擦除
	 */
	void onDEraseBtnEvent();

	/**
	 * 当前选择区域擦除（框选擦除）
	 */
	void onDAreaEraseBtnEvent();

	/**
	 * 清屏
	 */
	void onDClearScreenBtnEvent();

	/**
	 * 放大按钮事件
	 * @return 缩放大小0.5 - 3.0
     */
	float onDZoomOutBtnEvent();

	/**
	 * 缩小按钮
	 * @return 缩放大小 0.5 - 3.0
     */
	float onDZoomInBtnEvent();

	/**
	 * 左旋转按钮事件
	 */
	void onDRotateLeftBtnEvent();

	/**
	 * 右旋转事件
 	 */
	void onDRotateRightBtnEvent();

	/**
	 * 高度自适按钮
	 */
	void onDHeightSelfBtnEvent();

	/**
	 * 宽度自适应
	 */
	void onDWidthSelfBtnEvent();

    /**
	 * 1:1按钮事件
	 */
	void onDOneToOneBtnEvent();

	/**
	 * 白板名称栏，关闭白板按钮
	 */
   void onDCloseWbBtnEvent();

	/**
	 * 白板预览关闭白板按钮
	 * @param page
     */
	void onDCloseWbBtnEvent(IPage page);

	/**
	 * 确认删除白板
	 * @param page
	 */
	void onDIsCloseWbSureBtnEvent(IPage page);

	/**
	 * 删除所有白板
	 */
	void onDCloseAllWbBtnEvent();

	/**
	 * 文件存储设备选择对话框 "保存"按钮事件
	 * @param path
	 */
	void onDSwitchDeviceSaveBtnEvent(String path);

	/**
	 * 白板背景色选择
	 * @param color 当前选择的颜色
     */
	void onDSwitchBackGroundColor(int color);

	/**
	 * 选择子页
	 * @param pageNum 当前选择子页页码
     */
	void onDSelectSubPageNum(int pageNum);

	/**
	 * 切换白板
	 * @param page  白板Id
     */
	void onDSelectPage(IPage page);

	/**
	 * 打开 文件选择器
	 */
	void onDMenuOpenFileBtnEvent();

	/**
	 *菜单 中保存按钮事件
	 */
	void onDMenuSaveBtnEvent();

    /**
	 * 菜单 邮件分享按钮事件
	 */
	void onDMenuSendMailBtnEvent();

	/**
	 * 菜单 中扫描二维码按钮事件
	 */
	void onDMenuScanQRBtnEvent();

    /**
	 * 菜单 更换背景按钮事件
	 */
   void onDMenuChangeBgBtnEvent();

	/**
	 * 菜单 退出按钮事件
	 */
	void onDMenuExitBtnEvent();

	/**
	 * 保存对话框 保存按钮事件    --->  判断文件是否存在
	 * @param isSaveAll 是否保存全部
     */
	void onDSaveBtnEvent(String fileName,boolean isSaveAll);

    /**
	 * 保存对话框 取消按钮事件
	 */
	void onDSaveDilaogCancelBtnEvent();

	void onDSaveAllPageBtnEvent(String path,String dirName);

	/**
	 * 确认保存对话框 确认按钮事件
	 */
	void onDIsSaveDialogSurBtnEvent();

	/**
	 * 确认保存对话框，取消按钮事件
	 */
	void onDIsSaveDialogCancelBtnEvent();

	/**
	 * 关闭保存取消按钮事件
	 */
	void onDCloseSelectSaveWbDiloagCancelBtnEvent();

	/**
	 * 发送邮件按钮事件
	 * @param mailTitle 邮件标题
	 * @param mails 收件人数组
     */
	void onDSendMailBtnEvent(String mailTitle,String[] mails);
	/**
	 * 文件选择返回
	 * @param filePath
     */
	void onDSwitchFile(String filePath);

	/**
	 * 去邮箱服务器设置界面
	 */
	void onDToMailConfigSetting();

	/**
	 * 跳转到网络设置
	 */
    void onDToSettingNetwork();

	/**
	 * 释放管理权限
	 */
	void onDRlsDcsMnger();

	/**
	 * 当前会议模式改变：主讲/讨论
	 * @param mode
	 */
	void onDDataConfModeChange(int mode);


	/**
	 * 跳转到文件管理
	 */
	void onDToFileManager(String path);

	void onDilaogDismiss();

	void onDRejectApplyChairman();//拒绝管理员申请

	void onDAgreeApplyChairman(ApplyChairNtf acn);// 同意管理员申请
}
