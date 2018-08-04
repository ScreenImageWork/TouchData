package com.kedacom.touchdata.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import android.os.Handler;
import android.util.Log;

import com.kedacom.tplog.TPLog;
import com.kedacom.utils.StorageMangerJarUtils;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

public class MailUtil  {

	private final static long SEND_MAIL_TIMEOUT = 20*1000;

	static int port = 25;

	static String server = "smtp.kedacom.com";//邮件服务器mail.cpip.net.cn

	static String from = "数据协作";//发件人姓名,显示的发件人名字

	public static String subject = "会议纪要";

	static String user = "tptest@kedacom.com";//发送邮件人的地址

	static String password = "kdvtptest001";//密码

	private  static Timer mTimer = new Timer();

	public static boolean sendEmail(String email, String subject, String body) throws UnsupportedEncodingException {
		boolean result = true;

		String from = null;
		if(VersionUtils.isImix()){
			from = StorageMangerJarUtils.getImixDevicesName();
		}else{
			from = MailUtil.from;
		}

		try {
			Properties props = new Properties();
			props.put("mail.smtp.host", server);
			props.put("mail.smtp.port", String.valueOf(port));
			props.put("mail.smtp.auth", "true");
			Transport transport = null;
			Session session = Session.getDefaultInstance(props, null);
			transport = session.getTransport("smtp");
			transport.connect(server, user, password);
			MimeMessage msg = new MimeMessage(session);
			msg.setSentDate(new Date());
			InternetAddress fromAddress = new InternetAddress(user,from,"UTF-8");
			msg.setFrom(fromAddress);
			InternetAddress[] toAddress = new InternetAddress[1];
			toAddress[0] = new InternetAddress(email);
			msg.setRecipients(Message.RecipientType.TO, toAddress);
			msg.setSubject(subject, "UTF-8");
			msg.setText(body, "UTF-8");
			msg.saveChanges();
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();//关闭连接
			result = true;
		} catch (NoSuchProviderException e) {
			result = false;
			e.printStackTrace();
		} catch (MessagingException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 发送图片邮件，非附件
	 * @param email  收件人地址
	 * @param filePaths 文件路径
	 */
	public static void sendImageMail( final String email[],final String subject, final String filePaths[]){

		new Thread(){
			@Override
			public void run() {

					String from = null;
					if(VersionUtils.isImix()){
						from = StorageMangerJarUtils.getImixDevicesName();
					}else{
						from = MailUtil.from;
					}

				    startTimer();
				    TPLog.printKeyStatus("开始发送邮件。。。");
					Properties props = new Properties();
					props.put("mail.smtp.host", server);
					props.put("mail.smtp.port", String.valueOf(port));
					props.put("mail.smtp.auth", "true");
					Transport transport = null;
					Session session = Session.getDefaultInstance(props, null);
				   try {
					   transport = session.getTransport("smtp");
				   }catch(NoSuchProviderException e){//如果这里出现异常说明，配置参数有问题了
					   TPLog.printError("邮箱配置参数异常->"+e.toString());
					   checkException(e.toString());
					   cancelTimer();
					   return;
				   }

				    TPLog.printKeyStatus("邮件配置参数设置完成。。。");
					transport.addTransportListener(mTransportListener);

					try {
						transport.connect(server, user, password);
					}catch(MessagingException e){//如果这里出现异常说明，配置参数有问题了
						TPLog.printError("连接邮箱服务器异常->"+e.toString());
						checkException(e.toString());
						cancelTimer();
						return;
					}

				    if(!transport.isConnected()){
						mListener.onSendMailFailed();
						cancelTimer();
						return;
					}
				   TPLog.printKeyStatus("连接邮件服务器成功。。。");

				   try{
					Message message = new MimeMessage(session);
					message.setSentDate(new Date());
					message.setFrom(new InternetAddress(user,from,"UTF-8"));

					InternetAddress[] toAddress = new InternetAddress[email.length];

					for(int i=0;i<email.length;i++) {
						toAddress[i] = new InternetAddress(email[i]);
					}

					message.setRecipients(Message.RecipientType.TO, toAddress);
				
					// 创建关联的多媒
					//MimeMultipart multipart = new MimeMultipart("multipart/related");
//					MimeMultipart multipart = new MimeMultipart("related");
//					   MimeMultipart multipart = new MimeMultipart("related");//图文
					   MimeMultipart multipart = new MimeMultipart("mixed");//附件

					   // 创建邮件体HTML部分  2018.07.10 移除正文部分，将直接显示图片改为添加附件
//					   String htmlText = "<H1></H1>" +  "<p>";
//					   BodyPart messageBodyPart1 = new MimeBodyPart();
//
//					   for(int i=0;i<filePaths.length;i++){
//					   	htmlText = htmlText + "<img src=\"cid:memememe"+i+"\"/>"+"</p>";
//					   }
//
//					   messageBodyPart1.setContent(htmlText, "text/html");
//					   multipart.addBodyPart(messageBodyPart1);

					for(int i=0;i<filePaths.length;i++){
						//创建邮件体img部分
						MimeBodyPart  messageBodyPart2 = new MimeBodyPart();
						DataSource fds = new FileDataSource(filePaths[i]);
						messageBodyPart2.setDataHandler(new DataHandler(fds));
//						messageBodyPart2.setHeader("Content-ID","<memememe"+i+">");
						//添加文件名的话会出现附件
						messageBodyPart2.setFileName(MimeUtility.encodeWord(new File(filePaths[i]).getName()));

						multipart.addBodyPart(messageBodyPart2);
					}
					
						message.setContent(multipart);
						message.setSubject(subject);
						//message.setText(body);
						transport.sendMessage(message, message.getAllRecipients());
						transport.close();
					   TPLog.printKeyStatus("发送邮件成功");
					   cancelTimer();
				}catch(Exception e){
					TPLog.printError("发送邮件出现异常：");
					TPLog.printError(e);
					if(mListener!=null){
						mListener.onSendMailFailed();
					}
					   cancelTimer();
					e.printStackTrace();
				}
			}
		}.start();

	}


	public static boolean isConfigMail(){
		if(isNullOrEmpty(server)||isNullOrEmpty(user)||isNullOrEmpty(password)){
			return false;
		}
		if(!Utils.checkEmail(user)){
			return false;
		}
		if("0.0.0.0".equals(server)&&"Nexvision@kedacom.com".equals(user)&&"NexVision".equals(password)){
			return false;
		}
		return true;
	}

	/**
	 * 配置邮件信息
	 * @param server 邮件服务器的地址  :smtp.exmail.qq.com
	 * @param sendMailAccount 发送者账号
	 * @param sendMialPwd   发送者密码
	 */
	public static void configMail(String server,String sendMailAccount,String sendMialPwd){
		MailUtil.server = server;
		MailUtil.user = sendMailAccount;
		MailUtil.password = sendMialPwd;
	}

	private static OnSendMailListener mListener;
	public static void setOnSendMailListener(OnSendMailListener listener){
		mListener = listener;
	}

	public interface OnSendMailListener{
		void onSendMailSuccess();  //邮件发送成功
		void onSendMailFailed();    //邮件发送失败
		void onSendMailUnknownHost();  //邮箱服务器地址不正确
		void onConnectMailServerFailed(); //发件人账号配置不正确
		void onSendMailAuthenticationFailed(); //发件人账号配置不正确
	}

	public static void main(String args[]) throws UnsupportedEncodingException
	{
		MailUtil mMailUtil = new MailUtil();
		//sendImageMail(new String[]{"zhanglei_sxcpx@kedacom.com"},"测试邮件","/mnt/sdcard/1.png");//收件�?
	}
	
	
	private static boolean isNullOrEmpty(String args){
		if(args==null||args.equals("")){
			return true;
		}
		return false;
	}


	private static void checkException(String eStr){
		if(eStr==null){
			return;
		}
		if(eStr.contains("java.net.UnknownHostException")){//邮箱服务器地址不正确
			if(mListener!=null){
				mListener.onSendMailUnknownHost();
			}
		}else if(eStr.contains("javax.mail.AuthenticationFailedException")){//邮箱账号密码不正确
			if(mListener!=null){
				mListener.onSendMailAuthenticationFailed();
			}
		}else if(eStr.contains("javax.mail.MessagingException")){
			if(mListener!=null){
				mListener.onConnectMailServerFailed();
			}
		}else{
			if(mListener!=null){
				mListener.onSendMailFailed();
			}
		}
	}

	private static TransportListener mTransportListener = new TransportListener(){
	@Override
	public void messageDelivered(TransportEvent transportEvent) {
		TPLog.printKeyStatus("发送邮件成功!");
		if(mListener!=null){
			mListener.onSendMailSuccess();
		}
	}

	@Override
	public void messageNotDelivered(TransportEvent transportEvent) {
		TPLog.printKeyStatus("邮箱地址不正确!");
		if(mListener!=null){
			mListener.onSendMailFailed();
		}
	}

	@Override
	public void messagePartiallyDelivered(TransportEvent transportEvent) {
		TPLog.printKeyStatus("部分邮件发送失败!");
		if(mListener!=null){
			mListener.onSendMailFailed();
		}
	}
	};

	public static void callBackSendMailFailed(){
		cancelTimer();
		if(mListener!=null){
			mListener.onSendMailFailed();
		}
	}


	public static void startTimer(){
		if(mTimer==null){
			mTimer = new Timer();
		}
		mTimer.schedule(new MailTimerTask(),SEND_MAIL_TIMEOUT);
		TPLog.printKeyStatus("启动邮件发送定时器。。。");
	}

	public static void cancelTimer(){
		if(mTimer==null){
			return;
		}
		mTimer.cancel();
		mTimer = null;
		TPLog.printKeyStatus("取消邮件发送定时器。。。");
	}

	static class MailTimerTask extends TimerTask{
		@Override
		public void run() {
			if(mListener!=null){
				TPLog.printError("发送邮件超时！");
				mListener.onSendMailFailed();
				mTimer.cancel();
				mTimer = null;
			}
		}
	};

}