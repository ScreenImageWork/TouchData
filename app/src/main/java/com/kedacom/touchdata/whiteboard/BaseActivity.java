package com.kedacom.touchdata.whiteboard;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.kedacom.app.TouchDataApp;
import com.kedacom.storagelibrary.model.StorageItem;
import com.kedacom.touchdata.R;
import com.kedacom.touchdata.net.ConnectManager;
import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.dialog.ApplyChairDialog;
import com.kedacom.touchdata.whiteboard.dialog.CenterHintToast;
import com.kedacom.touchdata.whiteboard.dialog.CloseSelectSaveWbDiloag;
import com.kedacom.touchdata.whiteboard.dialog.ForbidDialog;
import com.kedacom.touchdata.whiteboard.dialog.IDialogBtnEvent;
import com.kedacom.touchdata.whiteboard.dialog.IsCloseWbDialog;
import com.kedacom.touchdata.whiteboard.dialog.IsSureExitDialog;
import com.kedacom.touchdata.whiteboard.dialog.LoadDialog;
import com.kedacom.touchdata.whiteboard.dialog.ProgressDialog;
import com.kedacom.touchdata.whiteboard.dialog.MoreMenuDialog;
import com.kedacom.touchdata.whiteboard.dialog.NetworkSettingDialog;
import com.kedacom.touchdata.whiteboard.dialog.NetworkStateDialog;
import com.kedacom.touchdata.whiteboard.dialog.NewMenuDialog;
import com.kedacom.touchdata.whiteboard.dialog.OneBtnDialog;
import com.kedacom.touchdata.whiteboard.dialog.PageNumDialog;
import com.kedacom.touchdata.whiteboard.dialog.PageThumbnailDialog2;
import com.kedacom.touchdata.whiteboard.dialog.QRCodeDialog;
import com.kedacom.touchdata.whiteboard.dialog.RemoteDcsOverIsSaveDialog;
import com.kedacom.touchdata.whiteboard.dialog.SaveFileSuccessDialog;
import com.kedacom.touchdata.whiteboard.dialog.SaveProgressDialog;
import com.kedacom.touchdata.whiteboard.dialog.SendMailDialog;
import com.kedacom.touchdata.whiteboard.dialog.SwitchBackGroundDialog;
import com.kedacom.touchdata.whiteboard.dialog.SwitchColorDialog;
import com.kedacom.touchdata.whiteboard.dialog.SwitchEraseDialog;
import com.kedacom.touchdata.whiteboard.dialog.SwitchFileDilaog2;
import com.kedacom.touchdata.whiteboard.dialog.SwitchSaveDeviceDialog;
import com.kedacom.touchdata.whiteboard.dialog.ThreeBtnDialog;
import com.kedacom.touchdata.whiteboard.dialog.ToMailConfigSettingDialog;
import com.kedacom.touchdata.whiteboard.dialog.ToolsBarDialog;
import com.kedacom.touchdata.whiteboard.dialog.TowBtnDialog;
import com.kedacom.touchdata.whiteboard.dialog.WhiteBoardNameDialog;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.SaveDialog;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.utils.DateUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.FullHorizontalScreenToast;
import com.kedacom.utils.MyToast;
import com.kedacom.utils.NavigationBarHelp;
import com.kedacom.utils.NetworkUtil;
import com.kedacom.utils.VersionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends Activity  implements IDialogBtnEvent,PopupWindow.OnDismissListener {

	protected  final int SAVEALL_UNKNOWN = 0;
	protected final int SAVEALL_ONEBYONE = 1;//逐个保存
	protected final int SAVEALL_NOT_PROMPT = 2;//全部保存 不提示

	private PowerManager.WakeLock mWakeLock = null;

	/**
	 * SWITCHERASE 橡皮檫
	 */
	public enum DialogType{
		UNKUNOW,LOADFILE,SAVESUCCESS,FILEEXIST,QUITISSAVE,SURE,SWITCHDEVICE,SWITCHFILE,SWITCHCOLOR,
		SWITCHERASE,MOREMENU,PAGETHUMBNAIL,SWITCHBACKGROUND,MENU,QRCODE,SAVE,SENDMAIL,PAGENUM,ISSAVEFILE,
		SETTINGNETWORK,NETWORKSTATE,CLOSE_SELECT_SAVE,TO_MAIL_CONFIG,SAVE_PROGRESS,IS_SURE_EXIT,IS_SURE_CLOSE_WB,
		CENTER_HINT,REMOTE_DCS_OVER_SAVE,FORBID,APPLY_CHAIR
	}

	private DialogType curDialogType = DialogType.UNKUNOW;

	private ProgressDialog mProgressDialog;

	private SaveFileSuccessDialog mSaveFileSuccessDialog;

	private ThreeBtnDialog mThreeBtnDialog;

	private OneBtnDialog sureDialog;

	private SwitchSaveDeviceDialog mSwitchSaveDeviceDialog;

	private SwitchFileDilaog2 mSwitchFileDilaog;

	private SwitchColorDialog mSwitchColorDialog;

	private SwitchEraseDialog mSwitchEraseDialog;

	private MoreMenuDialog mMoreMenuDialog;

	private WhiteBoardNameDialog mWhiteBoardNameDialog;

	private PageThumbnailDialog2 mPageThumbnailDialog;

	private SwitchBackGroundDialog mSwitchBackGroundDialog;

	private NewMenuDialog mMenuDialog;

	private QRCodeDialog mQRCodeDialog;

	private SaveDialog mSaveDialog;

	private SendMailDialog mSendMailDialog;

	private PageNumDialog mPageNumDialog;

	private LoadDialog mLoadDialog;

	private TowBtnDialog mIsSaveFileDilaog;

	private NetworkSettingDialog mNetworkSettingDialog;

	private NetworkStateDialog mNetworkStateDialog;

	private CloseSelectSaveWbDiloag mCloseSelectSaveWbDiloag;

	private ToMailConfigSettingDialog mToMailConfigSettingDialog;

	private ToolsBarDialog mToolsBarDialog;

	private SaveProgressDialog mSaveProgressDialog;

	private IsSureExitDialog mIsSureExitDialog;

	private IsCloseWbDialog mIsCloseWbDialog;

	private CenterHintToast mCenterHintToast;

	private RemoteDcsOverIsSaveDialog mRemoteDcsOverIsSaveDialog;

	private ForbidDialog mForbidDialog;

	private ApplyChairDialog mApplyChairDialog;

	private String args;

	protected boolean isExit = false;

	protected IPage curSavePage;

	protected String savePath;

	protected String aliasSavePath;

	protected String saveDirName;

	protected boolean isSaveAll;

	protected boolean isCloseSave = false;

	protected int curSaveAllType = SAVEALL_UNKNOWN;

	protected List<IPage> saveQueue = new ArrayList<IPage>();

	protected boolean sendMail = false;  //

	protected boolean qRDownLoad = false; //

	protected String mailImageCacheDir;

	protected String mailTitle;

	protected String recMails[];

	protected boolean isUnConnectServer = false;

	protected boolean needSaveProgressDialog = false; //当前是否需要进行保存进度显示

	protected int curSaveProgress = 0;

	private long createTime = 0;

	/**
	 * 白板会议名称，发送邮件时使用，这里的名称时间取得是白板打开时的时间
	 * 不过后来跟朱志强沟通后他决定使用发送邮件时的当前时间，因此这里的白板名称暂时未使用
	 * 先留在这里吧，没准后面他们又会选择该策略
	 */
	private String curMeetingName = "";

	protected   boolean switchFileDialogShowing = false;

	protected boolean remoteDcsOverSave = false;

	protected boolean delAllWbFromUi = false;//终端反馈的消息没有办法判断删除所有是自己还是别人触发的，因此这里添加一个条件用来判断是否是本端通过界面触发的操作

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TPLog.printError("-> onCreate");
		createTime = System.currentTimeMillis();
		super.onCreate(savedInstanceState);
		if(!VersionUtils.isImix()) {
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		// getWindow().setBackgroundDrawable(new ColorDrawable(WhiteBoardUtils.curBackground));
		this.mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "kedacom_lock");

		initDialog();

		curMeetingName = DateUtils.getCurTime("MM月dd日HH时mm分白板会议纪要");
	}

	private void initDialog(){
		MyToast.getInstance().init(this);
		FullHorizontalScreenToast.getInstance().init(this);

		//这个比较特殊，因此在界面初始化的时候进行初始化
		if(mQRCodeDialog==null){
			mQRCodeDialog = new QRCodeDialog(this);
		}
	}

	  protected void onResume()
	  {
		  TPLog.printError("-> onResume");
	      super.onResume();

		  //隐藏导航栏
		  hideSystemUI();

		  DialogType localCurDialogType = curDialogType;

	    if (mWakeLock!=null&&!this.mWakeLock.isHeld()) {
			this.mWakeLock.acquire();
		}

		  showWhiteBoardNameDialog();

		  if(!VersionUtils.is55InchDevice()) {
			  if (!ConnectManager.getInstance().getConnecter().isConnect()) {

				  TPLog.printKeyStatus("数据会议服务器没有链接。。");

				  boolean netState = NetworkUtil.getNetworkState(this);

				  //boolean isConnectingServer = netState; //是否可以链接服务器

				  showNetworkStateDialog(netState, false);

				  //第一次打开程序的时候如果没有网络就弹出网络设置对话框
				  if (!netState && !WhiteBoardUtils.isAPPShowing) {
					  showNetworkSettingDialog();
				  }

				  if (localCurDialogType == DialogType.LOADFILE) {
					  curDialogType = localCurDialogType;
				  }
			  }
		  }

		  switchFileDialogShowing = false;
		  showToolsBar();

		  long curTime = System.currentTimeMillis();
		  TPLog.printKeyStatus("onCreate 到 onResume 耗时："+(curTime - createTime));
	  }


	protected void onPause()
	{
		TPLog.printError("-> onPause");
		super.onPause();
		if (this.mWakeLock.isHeld())
			this.mWakeLock.release();

		dismissDialog();

		dismissWhiteBoardNameDialog();

		dismissDialog(DialogType.NETWORKSTATE);

		dismissToolsBar();
	}


	@Override
	protected void onStop() {
		TPLog.printError("-> onStop");
		super.onStop();

//		dismissToolsBar();

		showSystemUI();

		if(loadDialogIsShowing()){
			return;
		}

		closeAllDialog();//在imix上总是出现一些神奇的事情

		if(mNetworkStateDialog!=null&&mNetworkStateDialog.isShow()){
			mNetworkStateDialog.dismiss();
		}

	}

	  public void toastMsg(final String msg){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				//mToast.cancel();
				if(WhiteBoardUtils.isAPPShowing) {
					MyToast.getInstance().setText(msg);
					if (!MyToast.getInstance().isShowing())
						MyToast.getInstance().show();
				}
			}
		});
	}

	public void fullHorizontalScreenToastMsg(final String msg){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				//mToast.cancel();
				FullHorizontalScreenToast.getInstance().setText(msg);
				if(!FullHorizontalScreenToast.getInstance().isShowing())
					FullHorizontalScreenToast.getInstance().show();
			}
		});
	}



	protected void setWbName(String name){
		if(mWhiteBoardNameDialog==null){
			mWhiteBoardNameDialog = new WhiteBoardNameDialog(this);
		}
		mWhiteBoardNameDialog.setWbName(name);
	}

	protected void setSelectPageIndex(int index){
		if(mPageNumDialog==null){
			mPageNumDialog = new PageNumDialog(this);
		}
		mPageNumDialog.setCurSelectPageNum(index);
	}

	protected void setCurProgress(int progress){
		if(mProgressDialog==null){
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelBtnListener(clickListener);
		}
		mProgressDialog.setProgress(progress);
	}

	protected void setProgressMax(int max){
		if(mProgressDialog==null){
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelBtnListener(clickListener);
		}
		mProgressDialog.setProgressMax(max);
	}

	protected void setProgressDialogHintText(String text){
		if(mProgressDialog!=null){
			mProgressDialog.setHintText(text);
		}
	}

	protected void setCurScale(float scale){
		if(mMoreMenuDialog==null){
			mMoreMenuDialog = new MoreMenuDialog(this);
		}
		mMoreMenuDialog.setCurScale(scale);
	}

	protected void setSaveDialogSaveAllBtnIsDisplay(boolean isDisplay){
		if(mSaveDialog==null){
			mSaveDialog = new SaveDialog(this);
		}
		mSaveDialog.setSaveAllBtnIsDisplay(isDisplay);
	}

	protected void setPageManager(PageManager mPageManager){
		if(mPageThumbnailDialog==null){
			mPageThumbnailDialog = new PageThumbnailDialog2(this);
		}
		mPageThumbnailDialog.setPageManager(mPageManager);
	}

	protected void setQRCodeCacheFileDir( final String title,final String subTitle,final String cacheDir){
		TPLog.printError("setQRCodeCacheFileDir begin...");
		if(mQRCodeDialog!=null){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TPLog.printError("mQRCodeDialog.setQRCacheFileDir");
					mQRCodeDialog.setQRCacheFileDir(title,subTitle,cacheDir);
				}
			});
		}else{
			TPLog.printError("mQRCodeDialog = null");
		}
		TPLog.printError("setQRCodeCacheFileDir end...");
	}

	protected void setToolsBarBtnClickListener(View.OnClickListener listener){
		if(mToolsBarDialog!=null){
			mToolsBarDialog.setBtnClick(listener);
		}
	}

	protected void resetSwitchEraseDialog(){
		if(mSwitchEraseDialog == null){
			mSwitchEraseDialog = new SwitchEraseDialog(this);
		}
		mSwitchEraseDialog.reset();
	}

	protected int getCurBackgroundColor(){
		if(mSwitchBackGroundDialog==null){
			mSwitchBackGroundDialog = new SwitchBackGroundDialog(this);
		}
		return mSwitchBackGroundDialog.getCurBackgroundColor();
	}

	public ToolsBarDialog getToolsBarDialog(){
		if(mToolsBarDialog == null){
			mToolsBarDialog = new ToolsBarDialog(this);
		}
		return mToolsBarDialog;
	}

	protected void closeAllDialog(){
		IControler dialogs[] = {
				mProgressDialog,mSaveFileSuccessDialog,mThreeBtnDialog,sureDialog,mSwitchSaveDeviceDialog
				,mSwitchFileDilaog,mSwitchColorDialog,mSwitchEraseDialog,mMoreMenuDialog,
				mPageThumbnailDialog,mSwitchBackGroundDialog,mMenuDialog,mQRCodeDialog,mSaveDialog,mSendMailDialog,
				mPageNumDialog,mNetworkSettingDialog,mCloseSelectSaveWbDiloag,mToolsBarDialog,mIsSureExitDialog
				,mIsCloseWbDialog,mCenterHintToast,mRemoteDcsOverIsSaveDialog,mCenterHintToast,mForbidDialog
		   };

		int index = 0;
		for(IControler dialog:dialogs){
			if(dialog==null){
				index ++;
				continue;
			}
			index ++;
			dialog.dismiss();
		}
	}

	protected boolean hasDialogShowing(){
		IControler dialogs[] = {
				mProgressDialog,mSaveFileSuccessDialog,mThreeBtnDialog,sureDialog,mSwitchSaveDeviceDialog
				,mSwitchFileDilaog,mSwitchColorDialog,mSwitchEraseDialog,mMoreMenuDialog,
				mPageThumbnailDialog,mSwitchBackGroundDialog,mMenuDialog,mQRCodeDialog,mSaveDialog,mSendMailDialog,
				mPageNumDialog,mNetworkSettingDialog,mCloseSelectSaveWbDiloag,mToMailConfigSettingDialog,mIsSureExitDialog
				,mIsCloseWbDialog,mForbidDialog
		};

		for(IControler dialog:dialogs){
			if(dialog==null){
				continue;
			}
			if(dialog.isShow()){
				return true;
			}
		}
		return false;
	}

	 protected void showLoadDialog(String msg){
		 if(mLoadDialog==null){
			 mLoadDialog = new LoadDialog(this);
		 }
		mLoadDialog.show(msg);
	  }

	 protected void dismissLoadDialog(){
		 if(mLoadDialog==null){
			 mLoadDialog = new LoadDialog(this);
		 }
		  mLoadDialog.dismiss();
	  }

	  protected boolean loadDialogIsShowing(){
		  if(mLoadDialog==null){
			  mLoadDialog = new LoadDialog(this);
		  }
		  return mLoadDialog.isShow();
	  }



	// This snippet hides the system bars.
	protected void hideSystemUI() {
		//隐藏导航栏
		if (Build.VERSION.SDK_INT < 16) {
			return;
		}
		if(!VersionUtils.isImix()){
			NavigationBarHelp.hideNavigation(this);
		}

	}

	// This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
	private void showSystemUI() {
		if(!VersionUtils.isImix()) {
			NavigationBarHelp.showNavigation(new View(this));
		}
	}

	protected void showWhiteBoardNameDialog(){
		if(mWhiteBoardNameDialog==null){
			mWhiteBoardNameDialog = new WhiteBoardNameDialog(this);
		}
		//TPLog.printKeyStatus("WhiteBoardNameDialog state："+(mWhiteBoardNameDialog.isShow()?"显示":"未显示 "));
		if(!mWhiteBoardNameDialog.isShow())
		mWhiteBoardNameDialog.show();
	}

	protected void dismissWhiteBoardNameDialog(){
		if(mWhiteBoardNameDialog==null){
			mWhiteBoardNameDialog = new WhiteBoardNameDialog(this);
		}
		mWhiteBoardNameDialog.dismiss();
	}

	protected void dismissNetworkStateDialog(){
		TPLog.printKeyStatus("->dismissNetworkStateDialog");
		if(mNetworkStateDialog!=null){
			mNetworkStateDialog.dismiss();
		}
	}


	protected void showNetworkStateDialog(boolean networkUnusable,boolean serverconnecting){
		if(mNetworkStateDialog==null){
			mNetworkStateDialog = new NetworkStateDialog(this);
		}
		if(mNetworkStateDialog.isShow())
			if((mNetworkStateDialog.isNetworkUnusable() == networkUnusable)&&(mNetworkStateDialog.isServerConnecting() == serverconnecting))
				return;
		mNetworkStateDialog.show(networkUnusable,serverconnecting);
		curDialogType = DialogType.NETWORKSTATE;
	}

	protected void showNetworkSettingDialog(){
		if(curDialogType!=DialogType.SETTINGNETWORK){
			dismissDialog();
		}
		if(mNetworkSettingDialog==null){
			mNetworkSettingDialog = new NetworkSettingDialog(this);
			mNetworkSettingDialog.setCancelBtnClickListener(clickListener);
		}
		if(!mNetworkSettingDialog.isShow())
			mNetworkSettingDialog.show();
		curDialogType = DialogType.SETTINGNETWORK;
	}

	protected void showIsSaveFileDilaog(String msg){
		if(mIsSaveFileDilaog==null){
			mIsSaveFileDilaog = new TowBtnDialog(this);
			mIsSaveFileDilaog.setSurBtnListener(clickListener);
			mIsSaveFileDilaog.setCancelBtnListener(clickListener);
		}
		mIsSaveFileDilaog.show(msg);
		curDialogType = DialogType.ISSAVEFILE;
	}

	protected void showCloseSelectSaveWbDiloag(String msg){
		if(mCloseSelectSaveWbDiloag==null){
			mCloseSelectSaveWbDiloag = new CloseSelectSaveWbDiloag(this);
		}
		if(mCloseSelectSaveWbDiloag.isShow()){
			return;
		}
		mCloseSelectSaveWbDiloag.show(msg);
		curDialogType = DialogType.CLOSE_SELECT_SAVE;
	}

	protected void showPageNumDialog(int maxNum,View anchor){
		if(mPageNumDialog==null){
			mPageNumDialog = new PageNumDialog(this);
		}
		mPageNumDialog.show(maxNum,anchor);
		curDialogType = DialogType.PAGENUM;
	}

	protected void showSendMailDialog(){
		if(mSendMailDialog==null){
			mSendMailDialog = new SendMailDialog(this);
		}
		mSendMailDialog.show();
		curDialogType = DialogType.SENDMAIL;
		dismissWhiteBoardNameDialog();
	}

	protected void showSaveDialog(){
		if(mSaveDialog==null){
			mSaveDialog = new SaveDialog(this);
		}
		if(isExit){
			mSaveDialog.setSaveBtnText("保存并退出");
		}else{
			mSaveDialog.setSaveBtnText("保存");
		}
		mSaveDialog.setCurSavePage(curSavePage);
		mSaveDialog.show(saveDirName,aliasSavePath);
		mSaveDialog.setSaveAll(isSaveAll);
		curDialogType = DialogType.SAVE;
		dismissWhiteBoardNameDialog();
	}


	protected void showQRCodeDialog(){
		if(mQRCodeDialog==null){
			mQRCodeDialog = new QRCodeDialog(this);
		}
		mQRCodeDialog.show();
		curDialogType = DialogType.QRCODE;
	}

	protected void showMenuDialog(View anchor){
		if(mMenuDialog==null){
			mMenuDialog = new NewMenuDialog(this);
		}
		mMenuDialog.show(anchor);
		curDialogType = DialogType.MENU;
	}

	protected void showSwitchBackGroundDialog(){
		if(mSwitchBackGroundDialog==null){
			mSwitchBackGroundDialog = new SwitchBackGroundDialog(this);
		}
		curDialogType = DialogType.SWITCHBACKGROUND;
		mSwitchBackGroundDialog.show();
	}

	protected void showToMailConfigSettingDialog(){
		curDialogType = DialogType.TO_MAIL_CONFIG;
		if(mToMailConfigSettingDialog == null){
			mToMailConfigSettingDialog = new ToMailConfigSettingDialog(this);
		}
		if(mToMailConfigSettingDialog.isShow()){
			return;
		}
		mToMailConfigSettingDialog.setMsg("发送失败，请配置正确的邮箱账户和密码！");
		mToMailConfigSettingDialog.show();
	}

	protected void showToMailConfigSettingDialog(String msg){
		showToMailConfigSettingDialog();
		mToMailConfigSettingDialog.setMsg(msg);
	}

	protected void showLoadFileFailedText(){
		if(mProgressDialog==null){
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelBtnListener(clickListener);
		}
		mProgressDialog.showLoadFileFailedText();
		curDialogType = DialogType.LOADFILE;
	}

	protected void showLoadFileDilaog(int max,int progress){
		if(mProgressDialog==null){
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelBtnListener(clickListener);
		}
		setProgressMax(max);
		setCurProgress(progress);
		if(!mProgressDialog.isShow())
			mProgressDialog.show();
		curDialogType = DialogType.LOADFILE;
	}

	protected void showSaveFileSuccessDialog(String filePath,String aliasSavePath){
		if(mSaveFileSuccessDialog==null){
			mSaveFileSuccessDialog = new SaveFileSuccessDialog(this);
//			mSaveFileSuccessDialog.setCancelBtnListener(clickListener);
//			mSaveFileSuccessDialog.setOpenFileDirBtnListener(clickListener);
		}
		dismissDialog();
		mSaveFileSuccessDialog.setSaveFilePath(filePath,aliasSavePath);
		mSaveFileSuccessDialog.show();
		curDialogType = DialogType.SAVESUCCESS;
	}

	protected void setSaveFileSuccessDialogBtnText(String text){
		if(mSaveFileSuccessDialog!=null) {
			mSaveFileSuccessDialog.setCancelBtnText(text);
			if(isExit){
				mSaveFileSuccessDialog.setTitleText("退出");
			}else{
				mSaveFileSuccessDialog.setTitleText("保存");
			}
		}
	}


	protected void showFileExistDialog(String hintMsg){
		if(mThreeBtnDialog==null){
			mThreeBtnDialog = new ThreeBtnDialog(this);
			mThreeBtnDialog.setBtn1Listener(clickListener);
			mThreeBtnDialog.setBtn2Listener(clickListener);
			mThreeBtnDialog.setBtn3Listener(clickListener);
		}
		mThreeBtnDialog.setTitle("保存");
		mThreeBtnDialog.setCurSavePage(curSavePage);
		mThreeBtnDialog.setBtnText("重命名", "替换", "取消");
		mThreeBtnDialog.setMsg(hintMsg);
		mThreeBtnDialog.setCurFileName(saveDirName);
		mThreeBtnDialog.show();
		curDialogType = DialogType.FILEEXIST;
	}

	protected void showQuitIsSaveFileDialog(){
		if(mThreeBtnDialog==null){
			mThreeBtnDialog = new ThreeBtnDialog(this);
			mThreeBtnDialog.setBtn1Listener(clickListener);
			mThreeBtnDialog.setBtn2Listener(clickListener);
			mThreeBtnDialog.setBtn3Listener(clickListener);
		}
		String str = "退出前是否保存所有白板？";
		mThreeBtnDialog.setTitle("退出");
		mThreeBtnDialog.setMsg(str);
		mThreeBtnDialog.setBtnText("保存", "退出", "取消");
		mThreeBtnDialog.show();
		curDialogType = DialogType.QUITISSAVE;
	}

	protected void showIsSureDialog(String msg){
		if(sureDialog==null){
			sureDialog = new OneBtnDialog(this);
			sureDialog.setBtnListener(clickListener);
		}
		sureDialog.setMsg(msg);
		if(!sureDialog.isShow()) {
			dismissDialog();
			sureDialog.show();
		}
		curDialogType = DialogType.SURE;
}

	protected void showSwitchSaveDeviceDialog(  List<StorageItem>  devices){
		if(mSwitchSaveDeviceDialog==null){
			mSwitchSaveDeviceDialog = new SwitchSaveDeviceDialog(this);
			mSwitchSaveDeviceDialog.setSureBtnListener(clickListener);
			mSwitchSaveDeviceDialog.setCancelBtnListener(clickListener);
		}
		mSwitchSaveDeviceDialog.show(devices);
		curDialogType = DialogType.SWITCHDEVICE;
	}

	protected void showSwitchFileDialog(){
		if(mSwitchFileDilaog==null){
			mSwitchFileDilaog = new SwitchFileDilaog2(this);
			mSwitchFileDilaog.setCancelBtnListener(clickListener);
			mSwitchFileDilaog.setOpenBtnListener(clickListener);
		}
		mSwitchFileDilaog.show();
		curDialogType = DialogType.SWITCHFILE;
	}
	protected void showSwitchColorDialog(View anchor){
		if(mSwitchColorDialog==null){
			mSwitchColorDialog = new SwitchColorDialog(this);
		}
		if(mSwitchColorDialog.isShow()){
			mSwitchColorDialog.dismiss();
			return;
		}
		curDialogType = DialogType.SWITCHCOLOR;
		mSwitchColorDialog.show(anchor);
	}

	protected void showSwitchEraseDialog(View anchor){
		if(mSwitchEraseDialog == null){
			mSwitchEraseDialog = new SwitchEraseDialog(this);
		}
		if(mSwitchEraseDialog.isShow()){
			mSwitchEraseDialog.dismiss();
			return;
		}
		curDialogType = DialogType.SWITCHERASE;
		mSwitchEraseDialog.show(anchor);
	}

	protected void showMoreMenuDialog(View anchor){
		if(mMoreMenuDialog==null){
			mMoreMenuDialog = new MoreMenuDialog(this);
		}
		curDialogType = DialogType.MOREMENU;
		mMoreMenuDialog.show(anchor);
	}

	protected void showPageThumbnailDialog(){
		if(mPageThumbnailDialog==null){
			mPageThumbnailDialog = new PageThumbnailDialog2(this);
		}
		curDialogType = DialogType.PAGETHUMBNAIL;
		mPageThumbnailDialog.show();
	}


	public void showToolsBar(){
		TPLog.printKeyStatus("白板工具栏显示。。。");
		if(mToolsBarDialog==null){
			mToolsBarDialog = new ToolsBarDialog(this);
		}
		if(!mToolsBarDialog.isShow()){
			mToolsBarDialog.show();
		}
	}

	public void showSaveProgressDialog(){
		if(mSaveProgressDialog == null){
			mSaveProgressDialog = new SaveProgressDialog(this);
		}
		if(!mSaveProgressDialog.isShow()) {
			mSaveProgressDialog.show();
		}
		curDialogType = DialogType.SAVE_PROGRESS;
	}

	public void showSaveProgressDialog(String text){
		if(mSaveProgressDialog == null){
			mSaveProgressDialog = new SaveProgressDialog(this);
		}
//		mSaveProgressDialog.setText(text);
		if(!mSaveProgressDialog.isShow()) {
			mSaveProgressDialog.show(text);
		}
		curDialogType = DialogType.SAVE_PROGRESS;
	}

	public void showIsSureExitDialog(){
		if(mIsSureExitDialog == null){
			mIsSureExitDialog = new IsSureExitDialog(this);
		}

		if(!mIsSureExitDialog.isShow()){
			mIsSureExitDialog.show("");
		}

		curDialogType = DialogType.IS_SURE_EXIT;
	}

	protected void showIsCloseWbDialog(IPage page){
		if(mIsCloseWbDialog==null){
			mIsCloseWbDialog = new IsCloseWbDialog(this);
		}
		if(!mIsCloseWbDialog.isShow()){
			mIsCloseWbDialog.show(page);
		}

		curDialogType = DialogType.IS_SURE_CLOSE_WB;
	}

	protected void showCenterHintToast(String text){
		if(mCenterHintToast==null){
			mCenterHintToast = new CenterHintToast(this);
		}
		mCenterHintToast.show(text);

		curDialogType = DialogType.CENTER_HINT;
	}

	protected void showRemoteDcsOverIsSaveDialog(){
		if(mRemoteDcsOverIsSaveDialog==null){
			mRemoteDcsOverIsSaveDialog = new RemoteDcsOverIsSaveDialog(this);
		}
		mRemoteDcsOverIsSaveDialog.show();
		curDialogType = DialogType.REMOTE_DCS_OVER_SAVE;
	}

	protected void showForbidDialog(){
		if(mForbidDialog==null){
			mForbidDialog = new ForbidDialog(this);
		}
		mForbidDialog.show();
		curDialogType = DialogType.FORBID;
	}

	protected void showApplyChairDialog(String msg, ApplyChairNtf acn){
		if(mApplyChairDialog==null){
			mApplyChairDialog = new ApplyChairDialog(this);
		}
		mApplyChairDialog.show(msg,acn);
		curDialogType = DialogType.APPLY_CHAIR;
	}

	protected void notifyPageThumbnailDataChanged(){
		if(mPageThumbnailDialog == null){
			return;
		}
		mPageThumbnailDialog.notifyDataChanged();
	}

	protected boolean dialogIsShowing(DialogType dialogType){
		switch(dialogType){
			case LOADFILE:
				if(mProgressDialog!=null)
				return mProgressDialog.isShow();
				break;
			case SAVESUCCESS:
				if(mSaveFileSuccessDialog!=null)
					return mSaveFileSuccessDialog.isShow();
				break;
			case FILEEXIST:
			case QUITISSAVE:
				if(mThreeBtnDialog!=null)
					return mThreeBtnDialog.isShow();
				break;
			case SURE:
				if(sureDialog!=null)
					return sureDialog.isShow();
				break;
			case SWITCHDEVICE:
				if(mSwitchSaveDeviceDialog!=null)
					return mSwitchSaveDeviceDialog.isShow();
				break;
			case SWITCHFILE:
				if(mSwitchFileDilaog!=null)
					return mSwitchFileDilaog.isShow();
				break;
			case SWITCHCOLOR:
				if(mSwitchColorDialog!=null)
					return mSwitchColorDialog.isShow();
				break;
			case SWITCHERASE:
				if(mSwitchEraseDialog!=null)
					return mSwitchEraseDialog.isShow();
				break;
			case MOREMENU:
				if(mMoreMenuDialog!=null)
					return mMoreMenuDialog.isShow();
				break;
			case PAGETHUMBNAIL:
				if(mPageThumbnailDialog!=null)
					return mPageThumbnailDialog.isShow();
				break;
			case SWITCHBACKGROUND:
				if(mSwitchBackGroundDialog!=null)
					return mSwitchBackGroundDialog.isShow();
				break;
			case MENU:
				if(mMenuDialog!=null)
					return mMenuDialog.isShow();
				break;
			case QRCODE:
				if(mQRCodeDialog!=null)
					return mQRCodeDialog.isShow();
				break;
			case SENDMAIL:
				if(mSendMailDialog!=null)
					return mSendMailDialog.isShow();
				break;
			case SAVE:
				if(mSaveDialog!=null)
					return mSaveDialog.isShow();
				break;
			case PAGENUM:
				if(mPageNumDialog!=null)
					return mPageNumDialog.isShow();
				break;
			case ISSAVEFILE:
				if(mIsSaveFileDilaog!=null)
					return mIsSaveFileDilaog.isShow();
				break;
			case SETTINGNETWORK:
				if(mNetworkSettingDialog!=null)
					return mNetworkSettingDialog.isShow();
				break;
			case NETWORKSTATE:
				if(mNetworkStateDialog!=null)
					return mNetworkStateDialog.isShow();
				break;
			case CLOSE_SELECT_SAVE:
				if(mCloseSelectSaveWbDiloag!=null){
					return mCloseSelectSaveWbDiloag.isShow();
				}
				break;
			case TO_MAIL_CONFIG:
				if(mToMailConfigSettingDialog!=null){
					return mToMailConfigSettingDialog.isShow();
				}
				break;
			case IS_SURE_EXIT:
				if(mIsSureExitDialog!=null){
					return mIsSureExitDialog.isShow();
				}
				break;
			case IS_SURE_CLOSE_WB:
				if(mIsCloseWbDialog!=null){
					return mIsCloseWbDialog.isShow();
				}
				break;
			case REMOTE_DCS_OVER_SAVE:
				if(mRemoteDcsOverIsSaveDialog!=null){
					return mRemoteDcsOverIsSaveDialog.isShow();
				}
				break;
			case FORBID:
				if(mForbidDialog!=null){
					return mForbidDialog.isShow();
				}
				break;
			case APPLY_CHAIR:
				if(mApplyChairDialog!=null){
					return mApplyChairDialog.isShow();
				}
				break;
		}
		return false;
	}

	public void dismissToolsBar(){
		TPLog.printKeyStatus("准备隐藏底部工具栏！");
		if(mToolsBarDialog!=null){
			mToolsBarDialog.dismiss();
			TPLog.printKeyStatus("底部工具栏隐藏完成！");
		}else{
			TPLog.printKeyStatus("底部工具栏隐藏失败！");
		}
	}

	protected void dismissDialog(){
		dismissDialog(curDialogType);
	}

	protected void dismissDialog(DialogType dialogType){
		switch(dialogType){
			case LOADFILE:
				if(mProgressDialog!=null)
					mProgressDialog.dismiss();
				break;
			case SAVESUCCESS:
				if(mSaveFileSuccessDialog!=null)
				mSaveFileSuccessDialog.dismiss();
				break;
			case FILEEXIST:
			case QUITISSAVE:
				if(mThreeBtnDialog!=null)
				mThreeBtnDialog.dismiss();
				break;
			case SURE:
				if(sureDialog!=null)
				sureDialog.dismiss();
				break;
			case SWITCHDEVICE:
				if(mSwitchSaveDeviceDialog!=null)
				mSwitchSaveDeviceDialog.dismiss();
				break;
			case SWITCHFILE:
				if(mSwitchFileDilaog!=null)
				mSwitchFileDilaog.dismiss();
				break;
			case SWITCHCOLOR:
				if(mSwitchColorDialog!=null)
				mSwitchColorDialog.dismiss();
				break;
			case SWITCHERASE:
				if(mSwitchEraseDialog!=null)
				mSwitchEraseDialog.dismiss();
				break;
			case MOREMENU:
				if(mMoreMenuDialog!=null)
				mMoreMenuDialog.dismiss();
				break;
			case PAGETHUMBNAIL:
				if(mPageThumbnailDialog!=null)
				mPageThumbnailDialog.dismiss();
				break;
			case SWITCHBACKGROUND:
				if(mSwitchBackGroundDialog!=null)
				mSwitchBackGroundDialog.dismiss();
				break;
			case MENU:
				if(mMenuDialog!=null)
				mMenuDialog.dismiss();
				break;
			case QRCODE:
				if(mQRCodeDialog!=null)
				mQRCodeDialog.dismiss();
				break;
			case SENDMAIL:
				if(mSendMailDialog!=null)
				mSendMailDialog.dismiss();
				break;
			case SAVE:
				if(mSaveDialog!=null)
				mSaveDialog.dismiss();
				break;
			case PAGENUM:
				if(mPageNumDialog!=null)
				mPageNumDialog.dismiss();
				break;
			case ISSAVEFILE:
				if(mIsSaveFileDilaog!=null)
				mIsSaveFileDilaog.dismiss();
				break;
			case SETTINGNETWORK:
				if(mNetworkSettingDialog!=null)
				mNetworkSettingDialog.dismiss();
				break;
			case NETWORKSTATE:
				if(mNetworkStateDialog!=null)
				mNetworkStateDialog.dismiss();
				break;
			case CLOSE_SELECT_SAVE:
				if(mCloseSelectSaveWbDiloag!=null){
					mCloseSelectSaveWbDiloag.dismiss();
				}
				break;
			case TO_MAIL_CONFIG:
				if(mToMailConfigSettingDialog!=null){
					mToMailConfigSettingDialog.dismiss();
				}
				break;
			case SAVE_PROGRESS:
				if(mSaveProgressDialog!=null)
					mSaveProgressDialog.dismiss();
				break;
			case IS_SURE_EXIT:
				if(mIsSureExitDialog!=null)
					mIsSureExitDialog.dismiss();
				break;
			case IS_SURE_CLOSE_WB:
				if(mIsCloseWbDialog!=null){
					mIsCloseWbDialog.dismiss();
				}
				break;
			case REMOTE_DCS_OVER_SAVE:
				if(mRemoteDcsOverIsSaveDialog!=null){
					mRemoteDcsOverIsSaveDialog.dismiss();
				}
				break;
			case FORBID:
				if(mForbidDialog!=null){
					mForbidDialog.dismiss();
				}
				break;
			case APPLY_CHAIR:
				if(mApplyChairDialog!=null){
					mApplyChairDialog.dismiss();
				}
				break;
		}
	}


	public View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View arg0) {
			dismissDialog();
			int id = arg0.getId();
			if(id == R.id.dialogCancelBtn){ //取消
				if(curDialogType == DialogType.ISSAVEFILE)
				onDIsSaveDialogCancelBtnEvent();
				else
					onDCancelBtnEvent(curDialogType);
			}else if(id == R.id.dialogSureBtn){ //确认
				if(DialogType.SWITCHDEVICE==curDialogType){
					onDSwitchDeviceSaveBtnEvent(mSwitchSaveDeviceDialog.getSelectDevPath());
				}else if(curDialogType == DialogType.SWITCHFILE){
					onDSwitchFile(mSwitchFileDilaog.getSelectFile());
				}else if(curDialogType == DialogType.ISSAVEFILE){
					onDIsSaveDialogSurBtnEvent();
				} else{
					onDSureBtnEvent(curDialogType);
				}

			}else if(id == R.id.dialogOpenFileDir){//打开文件位置
				onDOpenDirBtnEvent(mSaveFileSuccessDialog.getSaveFilePath());
			}else if( id == R.id.dialogBtn1){  //三个按钮的对话框
				if(curDialogType == DialogType.FILEEXIST){
					onDResetFileNameBtnEvent();
				}else if(curDialogType == DialogType.QUITISSAVE){
					onDExitSaveBtnEvent();
				}
			}else if( id == R.id.dialogBtn2){
				if(curDialogType == DialogType.FILEEXIST){
					onDReplaceFileBtnEvent();
				}else if(curDialogType == DialogType.QUITISSAVE){
					onDExitNotSaveBtnEvent();
				}
			}else if( id == R.id.dialogBtn3){
				if(curDialogType == DialogType.QUITISSAVE){//退出
					onDExitCancelBtnEvent();
				}else{ //取消
					onDCancelBtnEvent(curDialogType);
				}
			}
		}
	};

	@Override
	public void onDismiss() {
		if(curDialogType == DialogType.SENDMAIL&&!mWhiteBoardNameDialog.isShow()&&!mSendMailDialog.isShow()){
				showWhiteBoardNameDialog();
		}else
		if(curDialogType == DialogType.SAVE&&!mWhiteBoardNameDialog.isShow()&&!mSaveDialog.isShow()){
			   showWhiteBoardNameDialog();
		}else if((mSaveDialog!=null&&!mSaveDialog.isShow())&&(mSendMailDialog!=null&&!mSendMailDialog.isShow())){
			showWhiteBoardNameDialog();
		}

//		if(curDialogType == DialogType.SENDMAIL||curDialogType == DialogType.SAVE){
//			showToolsBar();
//		}

		onDilaogDismiss();
		//隐藏导航栏
		hideSystemUI();
	}


	@Override
	public void onBackPressed() {
//		if(loadDialogIsShowing()){
//			return;
//		}
//		if(hasDialogShowing()){
//			dismissDialog();
//			return;
//		}
		finish();
	}

	@Override
	protected void onDestroy() {
		TPLog.printError("-> onDestroy");
		super.onDestroy();
		destroy();
	}

	private void destroy(){

		TPLog.printKeyStatus("-> destory");

		saveQueue.clear();

		args = null;
		curSavePage = null;
		savePath = null;
		aliasSavePath = null;
		saveDirName = null;
		saveQueue = null;
		mailImageCacheDir = null;
		mailTitle = null;
		recMails = null;

		IControler controler[] = {
				mProgressDialog,
				mSaveFileSuccessDialog ,
				mThreeBtnDialog ,
				sureDialog ,
				mSwitchSaveDeviceDialog ,
				mSwitchFileDilaog ,
				mSwitchColorDialog ,
				mSwitchEraseDialog ,
				mMoreMenuDialog ,
				mWhiteBoardNameDialog ,
				mPageThumbnailDialog,
				mSwitchBackGroundDialog,
				mMenuDialog ,
				mQRCodeDialog,
				mSaveDialog,
				mSendMailDialog,
				mPageNumDialog,
				mLoadDialog,
				mIsSaveFileDilaog,
				mNetworkSettingDialog,
				mNetworkStateDialog,
				mCloseSelectSaveWbDiloag,
				mToMailConfigSettingDialog,
				mIsSureExitDialog,
				mIsCloseWbDialog,
				mCenterHintToast,
				mRemoteDcsOverIsSaveDialog,
				mForbidDialog,
				mApplyChairDialog
		};

		for(int i = 0;i<controler.length;i++){
			if(controler[i]==null){
				continue;
			}
			if(controler[i].isShow()){
				controler[i].dismiss();
			}
			controler[i].destory();
			controler[i] = null;
		}

		controler = null;

		MyToast.getInstance().destroy();
	}

    //获取当前会议名称
	public String getCurMeetingName(){
		return curMeetingName;
	}

//	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
//		String SYSTEM_REASON = "reason";
//		String SYSTEM_HOME_KEY = "homekey";
//		String SYSTEM_HOME_KEY_LONG = "recentapps";
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
//				String reason = intent.getStringExtra(SYSTEM_REASON);
//				if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
//					//表示按了home键,程序到了后台
//					//closeAllDialog();
//					Toast.makeText(getApplicationContext(), "home", Toast.LENGTH_SHORT).show();
//				}else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
//					//表示长按home键,显示最近使用的程序列表
//
//				}
//			}
//		}
//	};

}
