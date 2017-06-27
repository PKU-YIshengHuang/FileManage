package com.yimo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FileManagerActivity extends ListActivity{
	/***
	 * �ļ��������������г����ص������ļ�
	 */
	private TextView mPath;// ��������б����棬��ʾ��ǰ�ļ���·��
	private List items=null;// �����ʾ������
	private ArrayList<File> paths=null;// ����ļ���·��
	String npaths=new String("/sdcard"); // ���·��
	private final File rootPath=new File("/mnt/sdcard");// ������ֻ��ļ��洢ϵͳ�еĸ�Ŀ¼��ʵ�ʴ������ֻ��С�
	// private final File sdPath=Environment.getExternalStorageDirectory();
	private File currentPath=null;
	private Context context=null;

	// �ж�Ϊ���ļ�oo�еı�־
	private boolean isEmpty=false;

	// ����Sort����,��������
	private Sort sort=new Sort(SortType.STSTEMDEFULT);

	/*
	 * �����ж��ܷ����ճ�������ı�־��Ĭ��Ϊfalse״̬��
	 * �������"����"��CONSTANT_PASTE��ֵ��Ϊtrue��ճ����ɺ�����ֵ�ͱ�Ϊfalse��true���ܽ���ճ��������
	 * false�����ܽ���ճ��������
	 */
	private static boolean CONSTANT_PASTE=false;
	private File copyFile;

	/*
	 * �����ж��ܷ�����ƶ��ı�־��Ĭ��ֵΪtrue�� �������"�ƶ�"��CONSTANT_MOVE��Ϊfalse���ƶ�������ɺ�����ֵ�ͱ�Ϊtrue��
	 * true:�ܽ����ƶ������� false:���ܽ����ƶ�������
	 */
	private static boolean CONSTANT_MOVE=true;
	private File moveFile;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_list);
		mPath=(TextView)findViewById(R.id.mPath);
		// testEditText=(EditText)findViewById(R.id.testEdit);
		context=this;// Activity�������Context��

		getSubDirectory(rootPath);
		showActivity(rootPath);

		// ʵ��ListActivity���б���ĳ����¼���Ӧ��
		getListView().setOnItemLongClickListener(
				new AdapterView.OnItemLongClickListener()
				{
					// parent:��������¼��� AbsListView.
					// view:AbsListView�б��������ͼ.
					// position:��ͼ��һ���е�λ�ã�������.
					// id:�������Ŀ���� ID.
					// ʵ��ʱ�����Ҫ������ѡ����Ŀ���������ݣ����Ե��� getItemAtPosition(position).
					public boolean onItemLongClick(AdapterView parent,View view,
							int position,long id){
						final File selectedFile=paths.get(position);
						if(!selectedFile.canRead()){
							AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
							bui.setTitle("���棺");
							bui.setMessage("��Ŀǰû��Ȩ�޷��ʸ�Ŀ¼");
							bui.setIcon(android.R.drawable.ic_menu_info_details);
							bui.setPositiveButton("ȷ��",null);
							bui.create().show();
						}else{
							AlertDialog.Builder dialog=new Builder(FileManagerActivity.this);
							dialog.setTitle("������еĲ�����");
							dialog.setIcon(R.drawable.ic_launcher);
							dialog.setItems(new String[]{"��","ɾ��","������","����","�ƶ�","����"},
									new OnClickListener()
									{
										public void onClick(DialogInterface dialog,int which){
											// ���в�����
											switch(which){
											case 0:
												// �����˴���Ŀ
												// testEditText.setText("�����˴���Ŀ");
												openFile(selectedFile);
												break;
											case 1:
												// ������ɾ����Ŀ
												AlertDialog.Builder bui=new Builder(
														FileManagerActivity.this);
												bui.setTitle("���棺");
												String info;
												if(selectedFile.isDirectory())
													info="��ȷ����Ҫɾ����Ŀ¼��";
												else
													info="��ȷ����Ҫɾ�����ļ���";

												bui.setMessage(info);
												bui.setIcon(android.R.drawable.ic_menu_info_details);
												bui.setPositiveButton("ȷ��",new OnClickListener()
												{
													public void onClick(DialogInterface dialog,int which){
														// ɾ��ָ��·�������ļ���/�ļ�
														if(selectedFile.isDirectory()){
															delDir(selectedFile);
														}else
															delFile(selectedFile);
														getSubDirectory(currentPath);
														showActivity(currentPath);
													}
												});
												bui.setNegativeButton("ȡ��",null);
												bui.create().show();
												break;
											case 2:
												// ��������������Ŀ
												// testEditText.setText("��������������Ŀ");
												AlertDialog.Builder newNamedialog=new Builder(context);
												newNamedialog.setTitle("�����ƣ�");
												newNamedialog.setIcon(R.drawable.ic_launcher);
												final EditText edit=new EditText(context);
												edit.setHint(selectedFile.getName());
												newNamedialog.setView(edit);
												newNamedialog.setPositiveButton("ȷ��",
														new OnClickListener()
														{
															public void onClick(DialogInterface dialog,
																	int which){
																String newName=edit.getText().toString();
																// testEditText.setText(newName);
																if(newName==null)
																	;
																else{
																	// �ж��Ƿ��������������ļ����������в������⡣
																	final File file=new File(currentPath,newName);
																	if(file.exists()){
																		AlertDialog.Builder bui=new Builder(
																				FileManagerActivity.this);
																		bui.setTitle("���棺");
																		String message="";
																		if(file.isFile()){
																			message="���ļ��Ѵ��ڣ���ȷ����Ҫ������";
																		}else if(file.isDirectory()){
																			message="��Ŀ¼�Ѵ��ڣ���ȷ����Ҫ������";
																		}
																		bui.setMessage(message);
																		bui.setIcon(android.R.drawable.ic_menu_info_details);
																		bui.setPositiveButton("ȷ��",
																				new OnClickListener()
																				{
																					public void onClick(
																							DialogInterface dialog,int which){
																						// ɾ��ָ��·�������ļ���/�ļ�
																						if(file.isDirectory()){
																							delDir(file);
																							selectedFile.renameTo(file);
																						}else if(file.isFile()){
																							delFile(file);
																							selectedFile.renameTo(file);
																						}
																						getSubDirectory(currentPath);
																						showActivity(currentPath);
																						// paths.add(currentPath);
																					}
																				});
																		bui.setNegativeButton("ȡ��",null);
																		bui.create().show();
																	}else{
																		selectedFile.renameTo(file);
																		getSubDirectory(currentPath);
																		showActivity(currentPath);
																	}
																}
															}
														});
												newNamedialog.setNegativeButton("ȡ��",null);
												newNamedialog.create().show();
												break;
											case 3:
												// �����˸�����Ŀ
												copyFile=selectedFile;
												CONSTANT_PASTE=true;
												break;
											case 4:
												// �������ƶ���Ŀ
												// testEditText.setText("�������ƶ���Ŀ");
												if(CONSTANT_MOVE==true){
													moveFile=selectedFile;
													// delDir(selectedFile);
													CONSTANT_MOVE=false;
												}else{
													Toast.makeText(context,"���������ǰ���ƶ�������",
															Toast.LENGTH_SHORT).show();
												}
												break;
											case 5:
												// ������������Ŀ
												// testEditText.setText("������������Ŀ");
												if(selectedFile.isFile()){
													LayoutInflater inflater2=LayoutInflater.from(context);
													// dialog_set_merge��һ��RadioGroup
													// ����������ѡ��
													View dialog_view=inflater2.inflate(R.layout.listview,
															null); // ��ʱ����
													TextView show=(TextView)dialog_view
															.findViewById(R.id.view_dia_text);
													show.setText("   ��  ��    : "+selectedFile.getName()
															+"\n   ·  ��    : "+selectedFile.getAbsolutePath()
															+"\n�޸�ʱ�� : "
															+getFileLastModifiedTime(selectedFile)
															+"\n�ļ���С : "
															+FormatFileSize(selectedFile.length())
															+"\n�ļ����� : "+getType(selectedFile));
													AlertDialog helpDialog=new AlertDialog.Builder(
															context).setIcon(R.drawable.ic_launcher)
															.setTitle("    ����").setView(dialog_view)
															.setPositiveButton("ȷ��",new OnClickListener()
															{
																public void onClick(DialogInterface dialog,
																		int which){

																}
															}).create();
													helpDialog.show();
													break;
												}else{
													LayoutInflater inflater2=LayoutInflater.from(context);
													// dialog_set_merge��һ��RadioGroup
													// ����������ѡ��
													View dialog_view=inflater2.inflate(R.layout.listview,
															null); // ��ʱ����
													TextView show=(TextView)dialog_view
															.findViewById(R.id.view_dia_text);
													show.setText("   ��  ��    : "+selectedFile.getName()
															+"\n   ·  ��    : "+selectedFile.getAbsolutePath()
															+"\n�޸�ʱ�� : "
															+getFileLastModifiedTime(selectedFile)
															+"\n�ļ����� : �ļ���");
													AlertDialog helpDialog=new AlertDialog.Builder(
															context).setIcon(R.drawable.ic_launcher)
															.setTitle("    ����").setView(dialog_view) // �ϱ�
															.setPositiveButton("֪����",new OnClickListener()
															{
																public void onClick(DialogInterface dialog,
																		int which){

																}
															}).create();
													helpDialog.show();
													break;
												}
											}
										}
									});
							dialog.create().show();
							return true;
						}
						return true;
					}
				});
	}

	private static String getFileLastModifiedTime(File f){

		Calendar cal=Calendar.getInstance();
		long time=f.lastModified();
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cal.setTimeInMillis(time);
		// ������޸�ʱ��[2] 2009-08-17 10:32:38
		return formatter.format(cal.getTime());
	}

	public String FormatFileSize(long fileS){// ת���ļ���С

		DecimalFormat df=new DecimalFormat("#.00");
		String fileSizeString="";
		if(fileS<1024){
			fileSizeString=df.format((double)fileS)+"B";
		}else if(fileS<1048576){
			fileSizeString=df.format((double)fileS/1024)+"K";
		}else if(fileS<1073741824){
			fileSizeString=df.format((double)fileS/1048576)+"M";
		}else{
			fileSizeString=df.format((double)fileS/1073741824)+"G";
		}
		return fileSizeString;
	}

	private void openFile(File file){
		Intent intent=new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		String type=getMIMEType(file,0);
		intent.setDataAndType(Uri.fromFile(file),type);
		try{
			startActivity(intent);
		}catch(Exception e){
			Toast.makeText(this,"δ֪���ͣ����ܴ�",Toast.LENGTH_SHORT).show();
		}
	}

	// �õ��ļ����� ͨ������getMIME ֻ�õ�ǰ�ߵ����� ��/�� �����ȥ��
	private String getType(File file){
		String type=getMIMEType(file,0);
		String end=type.substring(0,type.indexOf("/"));
		return end;
	}

	private String getMIMEType(File file,int j){
		String type="*/*";
		String fileName=file.getName();
		int dotIndex=fileName.lastIndexOf('.');
		if(dotIndex<0){
			return type;
		}
		String end=fileName.substring(dotIndex,fileName.length()).toLowerCase();
		if(end.equals("")){
			return type;
		}
		for(int i=0;i<MIME_MapTable.length;i++){
			if(end.equals(MIME_MapTable[i][0])){
				type=MIME_MapTable[i][1];
			}
		}
		return type;
	}

	private final String[][] MIME_MapTable={
			// {��׺���� MIME����}
			{".3gp","video/3gpp"},
			{".apk","application/vnd.android.package-archive"},
			{".asf","video/x-ms-asf"},
			{".avi","video/x-msvideo"},
			{".bin","application/octet-stream"},
			{".bmp","image/bmp"},
			{".c","text/plain"},
			{".class","application/octet-stream"},
			{".conf","text/plain"},
			{".cpp","text/plain"},
			{".doc","application/msword"},
			{".docx",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
			{".xls","application/vnd.ms-excel"},
			{".xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
			{".exe","application/octet-stream"},
			{".gif","image/gif"},
			{".gtar","application/x-gtar"},
			{".gz","application/x-gzip"},
			{".h","text/plain"},
			{".htm","text/html"},
			{".html","text/html"},
			{".jar","application/java-archive"},
			{".java","text/plain"},
			{".jpeg","image/jpeg"},
			{".jpg","image/jpeg"},
			{".js","application/x-javascript"},
			{".log","text/plain"},
			{".m3u","audio/x-mpegurl"},
			{".m4a","audio/mp4a-latm"},
			{".m4b","audio/mp4a-latm"},
			{".m4p","audio/mp4a-latm"},
			{".m4u","video/vnd.mpegurl"},
			{".m4v","video/x-m4v"},
			{".mov","video/quicktime"},
			{".mp2","audio/x-mpeg"},
			{".mp3","audio/x-mpeg"},
			{".mp4","video/mp4"},
			{".mpc","application/vnd.mpohun.certificate"},
			{".mpe","video/mpeg"},
			{".mpeg","video/mpeg"},
			{".mpg","video/mpeg"},
			{".mpg4","video/mp4"},
			{".mpga","audio/mpeg"},
			{".msg","application/vnd.ms-outlook"},
			{".ogg","audio/ogg"},
			{".pdf","application/pdf"},
			{".png","image/png"},
			{".pps","application/vnd.ms-powerpoint"},
			{".ppt","application/vnd.ms-powerpoint"},
			{".pptx",
					"application/vnd.openxmlformats-officedocument.presentationml.presentation"},
			{".prop","text/plain"},{".rc","text/plain"},
			{".rmvb","audio/x-pn-realaudio"},{".rtf","application/rtf"},
			{".sh","text/plain"},{".tar","application/x-tar"},
			{".tgz","application/x-compressed"},{".txt","text/plain"},
			{".wav","audio/x-wav"},{".wma","audio/x-ms-wma"},
			{".wmv","audio/x-ms-wmv"},{".wps","application/vnd.ms-works"},
			{".xml","text/plain"},{".z","application/x-compress"},
			{".zip","application/x-zip-compressed"},{"","*/*"}};

	/*
	 * �����ļ����ļ��е�ͼ�� ��������Int 0����ʾͼ��Ϊ�ļ������� 1:��ʾͼ��Ϊ��������
	 */
	private int setIcons(File file,HashMap map){
		if(file.isDirectory()){
			map.put("ItemImage",R.drawable.folder);
			return 1;
		}
		// �ж��ļ�����
		String type="*/*";
		String fileName=file.getName();
		int dotIndex=fileName.lastIndexOf('.');
		if(dotIndex<0){
			map.put("ItemImage",R.drawable.defaults);
			return 0;
		}
		String end=fileName.substring(dotIndex,fileName.length()).toLowerCase();
		if(end.equals("")){
			map.put("ItemImage",R.drawable.defaults);
			return 0;
		}

		if(end.equals(".jpg")||end.equals(".gif")||end.equals(".bmp")
				||end.equals(".jpeg")){
			map.put("ItemImage",R.drawable.picture);
		}else if(end.equals(".asf")||end.equals(".avi")||end.equals(".3gp")
				||end.equals(".m4u")||end.equals(".m4v")||end.equals(".mov")
				||end.equals(".mp4")||end.equals(".mpe")||end.equals(".mpeg")
				||end.equals(".mpg")||end.equals(".mpg4")){
			map.put("ItemImage",R.drawable.video);
		}else if(end.equals(".c")||end.equals(".conf")||end.equals(".cpp")
				||end.equals(".h")||end.equals(".java")||end.equals(".log")
				||end.equals(".prop")||end.equals(".rc")||end.equals(".sh")
				||end.equals(".txt")||end.equals(".xml")){
			map.put("ItemImage",R.drawable.txt);
		}else if(end.equals(".m3u")||end.equals(".m4a")||end.equals(".m4b")
				||end.equals(".m4p")||end.equals(".mp2")||end.equals(".mp3")
				||end.equals(".mpga")||end.equals(".ogg")||end.equals(".rmvb")
				||end.equals(".wav")||end.equals(".wmv")){
			map.put("ItemImage",R.drawable.music);
		}else if(end.equals(".z")||end.equals(".zip")||end.equals(".gtar")
				||end.equals(".gz")||end.equals(".jar")||end.equals(".js")){
			map.put("ItemImage",R.drawable.zip);
		}else if(end.equals(".apk")||end.equals(".bin")||end.equals(".class")
				||end.equals(".doc")||end.equals(".docx")||end.equals(".xls")
				||end.equals(".xlsx")||end.equals(".exe")||end.equals(".mpc")
				||end.equals(".msg")||end.equals(".pdf")||end.equals(".rtf")
				||end.equals(".pps")||end.equals(".ppt")||end.equals(".pptx")
				||end.equals(".tar")||end.equals(".tgz")||end.equals(".wps")){
			map.put("ItemImage",R.drawable.application);
		}else{
			map.put("ItemImage",R.drawable.defaults);
		}

		return 1;
	}

	/**
	 * ����һ���ļ�,srcFileԴ�ļ���destFileĿ���ļ�
	 * 
	 * @param path
	 * @throws IOException
	 */
	public boolean copyFileTo(File srcFile,File destFile) throws IOException{
		if(srcFile.isDirectory()||destFile.isDirectory())
			return false;// �ж��Ƿ����ļ�
		FileInputStream fis=new FileInputStream(srcFile);
		FileOutputStream fos=new FileOutputStream(destFile);
		int readLen=0;
		byte[] buf=new byte[1024];
		while((readLen=fis.read(buf))!=-1){
			fos.write(buf,0,readLen);
		}
		fos.flush();
		fos.close();
		fis.close();
		return true;
	}

	/**
	 * ����Ŀ¼�µ������ļ���ָ��Ŀ¼
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean copyFilesTo(File srcDir,File destDir) throws IOException{
		if(!destDir.exists()){
			destDir.mkdirs();
		}
		if(!srcDir.isDirectory()||!destDir.isDirectory())
			return false;// �ж��Ƿ���Ŀ¼
		File[] srcFiles=srcDir.listFiles();
		for(int i=0;i<srcFiles.length;i++){
			if(srcFiles[i].isFile()){
				// ���Ŀ���ļ�
				File destFile=new File(destDir.getPath()+"/"+srcFiles[i].getName());
				copyFileTo(srcFiles[i],destFile);
			}else if(srcFiles[i].isDirectory()){
				File theDestDir=new File(destDir.getPath()+"/"+srcFiles[i].getName());
				copyFilesTo(srcFiles[i],theDestDir);
			}
		}
		return true;
	}

	/**
	 * �ƶ�һ���ļ���ָ���ļ�
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public boolean moveFileTo(File srcFile,File destFile) throws IOException{
		boolean iscopy=copyFileTo(srcFile,destFile);
		if(!iscopy)
			return false;
		// delDir(srcFile);
		delFile(srcFile);
		return true;
	}

	/**
	 * �ƶ�Ŀ¼�µ������ļ���ָ��Ŀ¼
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean moveFilesTo(File srcDir,File destDir) throws IOException{
		// ��ӵĴ��룺
		if(!destDir.exists()){
			destDir.mkdirs();
		}
		if(!srcDir.isDirectory()||!destDir.isDirectory()){
			return false;
		}
		File[] srcDirFiles=srcDir.listFiles();
		for(int i=0;i<srcDirFiles.length;i++){
			if(srcDirFiles[i].isFile()){
				File oneDestFile=new File(destDir.getPath()+"/"
						+srcDirFiles[i].getName());
				moveFileTo(srcDirFiles[i],oneDestFile);
				// delDir(srcDirFiles[i]);
				delFile(srcDirFiles[i]);
			}else if(srcDirFiles[i].isDirectory()){
				File oneDestFile=new File(destDir.getPath()+"/"
						+srcDirFiles[i].getName());
				moveFilesTo(srcDirFiles[i],oneDestFile);
				delDir(srcDirFiles[i]);
			}
		}
		// �Լ��ӵ�һ�д��룺
		srcDir.delete();
		return true;
	}

	/**
	 * ɾ��һ���ļ�
	 * 
	 * @param file
	 * @return
	 */
	public boolean delFile(File file){
		if(file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * ɾ��һ��Ŀ¼�������Ƿǿ�Ŀ¼��
	 * 
	 * @param dir
	 */
	public boolean delDir(File dir){
		if(dir==null||!dir.exists()||dir.isFile()){
			return false;
		}
		for(File file:dir.listFiles()){
			if(file.isFile()){
				file.delete();
			}else if(file.isDirectory()){
				delDir(file);// �ݹ�
			}
		}
		dir.delete();
		return true;
	}

	/*** ����ļ��ܹ���һ������***����һ���������ļ�·�� ***/
	private void getSubDirectory(File directory){

		currentPath=directory;
		npaths=directory.getParent();
		items=new ArrayList();
		paths=new ArrayList<File>();
		File[] files=directory.listFiles();
		// ��ָ������ʽ����
		sort.sort(files);
		// judge directory-------
		if(!directory.equals(rootPath)){
			// list���ĵ�һ�м�һ��ѡ���ת����Ŀ¼
			HashMap<String, Object> map=new HashMap<String, Object>();
			map.put("ItemTitle","go back "+rootPath.getPath()); // ����
			map.put("ItemImage",R.drawable.ic_launcher);// ͼƬ
			items.add(map);
			paths.add(rootPath);//

			// ��ת���ϼ�Ŀ¼
			map=new HashMap<String, Object>();
			map.put("ItemTitle"," last directory"); // ����
			map.put("ItemImage",R.drawable.ic_launcher);// ͼƬ
			items.add(map);
			paths.add(directory.getParentFile());
		}
		if(files.length>0){
			/** ���ļ����ŵ�list������ ***/
			for(int i=0;i<files.length;i++){
				File file=files[i];
				// addListElement(file.getName());
				HashMap<String, Object> map=new HashMap<String, Object>();
				// map.put("ItemTitle",file.getName()); // ����
				String showContent=file.getName()+"\n"
						+String.format("%tY/%<tm/%<td",new Date(file.lastModified()));
				if(file.isFile()){
					showContent+="   "+FormatFileSize(file.length());
				}
				map.put("ItemTitle",showContent); // ����
				setIcons(file,map);
				// String.format("%tY/%tm/%td",new Date(file.lastModified()));
				// new DateHandle(file.lastModified()).getTime()
				// map.put("ItemImage",R.drawable.ic_launcher);// ͼƬ
				items.add(map);
				paths.add(file);
			}
			isEmpty=false;
		}else{
			isEmpty=true;
		}
	}

	void showActivity(File directory){
		// ���ļ�·����ʾ��listView�Ϸ���
		mPath.setText("��ǰ·����"+directory.getPath());
		mPath.setTextSize(15f);
		// ///**дһ�����������������е����ݣ��ļ�������ӵ�ListView�ؼ�����ʾ��������ʽΪ�Լ������һ��xml�ļ�***/
		// ArrayAdapter fileList=new ArrayAdapter(getApplication(),
		// R.layout.listview,items);
		// setListAdapter(fileList); //���� filelist
		// ////ʹ��getApplication(),��������ã����Ǵ������ǵ�Ӧ�ó�����࣬ʹ�������Ի�õ�ǰӦ�õ����⣬��Դ�ļ��е����ݵ�
		// //r.layout.file_rows ����������ʾ items������ʾ����
		// //������������Item�Ͷ�̬�����Ӧ��Ԫ��
		SimpleAdapter adapter=new SimpleAdapter(this,items,// ����Դ��List<?
				// extends
				// Map<String,?>>����
				R.layout.file_rows,// ListItem��XML����ʵ��
				// //��̬������ImageItem��Ӧ������
				new String[]{"ItemTitle","ItemImage"},
				// ImageItem��XML�ļ������һ��ImageView,����TextView ID
				new int[]{R.id.ItemTitle,R.id.ItemImage});
		setListAdapter(adapter);
		if(this.isEmpty==true){
			Toast.makeText(this,"��ǰ�ļ���û���ļ�",Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onListItemClick(ListView l,View v,int position,long id){
		File selectedFile=paths.get(position);
		if(selectedFile.canRead()){
			if(selectedFile.isDirectory()){
				// ------������ļ��У���������getFIleDir()�����򿪣����򵯳��Ի���һö
				getSubDirectory(selectedFile);
				showActivity(selectedFile);
			}else{
				openFile(selectedFile);
			}
		}else{
			AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
			bui.setTitle("���棺");
			bui.setMessage("��û��Ȩ�޷��ʸ�Ŀ¼��");
			bui.setIcon(android.R.drawable.ic_menu_info_details);//
			bui.setPositiveButton("ȷ��",null);
			bui.create().show();
		}
	}

	// ����menu��Activity����onCreateOptionsMenu(Menu menu)��������һ��ʵ��Menu�ӿڵ�menu���󣬹�ʹ�á�
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// menu.add()�������ĸ����������ǣ�
		// ��һ�������
		// �ڶ�����ID����menuʶ���ţ���ʶ��menu�õġ�����Ҫ��
		// ��������˳����������Ĵ�С�����˵����ֵ��Ⱥ�˳��˳���ǲ�����С���󣬲˵������ң����ϵ������С�һ�����������
		// ���ĸ�����ʾ�ı���
		menu.add(0,Menu.FIRST,0,"�½�Ŀ¼").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0,Menu.FIRST+1,1,"ճ��").setIcon(android.R.drawable.ic_menu_save);
		menu.add(0,Menu.FIRST+2,2,"����").setIcon(
				android.R.drawable.ic_menu_sort_by_size);
		menu.add(1,Menu.FIRST+3,3,"����ƶ�").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(1,Menu.FIRST+4,4,"�˳�").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(1,Menu.FIRST+5,5,"����").setIcon(android.R.drawable.ic_menu_help);
		// ֻ�з���True�Ż������á�
		return true;
	}

	// �˵���ѡ���¼� toastֻ����ʾһ����̵���Ϣ
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case Menu.FIRST:
			// Toast.makeText(this, "�½�Ŀ¼�˵��������", Toast.LENGTH_SHORT).show();
			AlertDialog.Builder newDialog=new Builder(FileManagerActivity.this);
			newDialog.setMessage("�½�Ŀ¼����");
			newDialog.setIcon(R.drawable.ic_launcher);
			final EditText edit=new EditText(this);
			edit.setHint("�½�Ŀ¼");
			newDialog.setView(edit);
			newDialog.setPositiveButton("ȷ��",new OnClickListener()
			{
				public void onClick(DialogInterface dialog,int which){
					if(currentPath.canWrite()){
						// ����½�Ŀ¼
						String directoryName=edit.getText().toString();
						File file=new File(currentPath,directoryName);
						if(file.exists()){
							AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
							bui.setTitle("���棺");
							bui.setMessage("��Ŀ¼�Ѵ��ڣ��������ظ�������");
							bui.setIcon(android.R.drawable.ic_menu_info_details);
							bui.setPositiveButton("ȷ��",null);
							bui.create().show();
						}else{
							// ������Ŀ¼λ���б�ĵĵ�һ�У���������������������ؼ�.
							file.mkdir();
							getSubDirectory(currentPath);
							showActivity(currentPath);
							paths.add(currentPath);
						}
					}else{
						AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
						bui.setTitle("���棺");
						bui.setMessage("��Ŀǰû��Ȩ�޽����½��ļ��в�����");
						bui.setIcon(android.R.drawable.ic_menu_info_details);
						bui.setPositiveButton("ȷ��",null);
						bui.create().show();
					}
				}
			});
			newDialog.setNegativeButton("ȡ��",null);
			newDialog.create().show();
			break;
		case Menu.FIRST+1:
			// �ж��ܷ����ճ��������
			if(CONSTANT_PASTE==false){
				// һ�����ĸ��ո�.
				Toast.makeText(this,"������Ϊ�գ�����ճ��\n    ���Ƚ��и��Ʋ���",Toast.LENGTH_SHORT)
						.show();
			}else{
				final File destDir=new File(currentPath,copyFile.getName());
				final boolean isDirectory=copyFile.isDirectory();
				String message="";
				if(isDirectory){
					message="��Ŀ¼�Ѵ��ڣ���ȷ����Ҫ��������";
				}else{
					message="���ļ��Ѵ��ڣ���ȷ����Ҫ��������";
				}
				// �ж��Ƿ���Ŀ¼�ظ����ļ��ظ�����
				if(destDir.exists()){
					AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
					bui.setTitle("���棺");
					bui.setMessage(message);
					bui.setIcon(android.R.drawable.ic_menu_info_details);
					bui.setPositiveButton("ȷ��",new OnClickListener()
					{
						public void onClick(DialogInterface dialog,int which){
							try{
								if(isDirectory){
									delDir(destDir);
									copyFilesTo(copyFile,destDir);
								}else{
									copyFileTo(copyFile,destDir);
								}
								CONSTANT_PASTE=false;
							}catch(IOException e){
								CONSTANT_PASTE=false;
								e.printStackTrace();
							}
							getSubDirectory(currentPath);
							showActivity(currentPath);
						}
					});
					bui.setNegativeButton("ȡ��",null);
					bui.create().show();
				}else{
					try{
						if(isDirectory)
							copyFilesTo(copyFile,destDir);
						else{
							copyFileTo(copyFile,destDir);
						}
						CONSTANT_PASTE=false;
					}catch(IOException e){
						CONSTANT_PASTE=false;
						e.printStackTrace();
					}
					getSubDirectory(currentPath);
					showActivity(currentPath);
				}
			}
			break;

		case Menu.FIRST+2:
		//	Toast.makeText(this,"����˵��������",Toast.LENGTH_SHORT).show();
			AlertDialog.Builder dialog=new Builder(FileManagerActivity.this);
			dialog.setTitle("������еĲ�����");
			dialog.setIcon(R.drawable.ic_launcher);
			dialog.setItems(new String[]{"ϵͳĬ��","����޸�ʱ��","����","�ļ������ļ�","�ļ���С","����"},
					new OnClickListener()
					{
						public void onClick(DialogInterface dialog,int which){
							// ���в�����which�±��0��ʼ
							switch(which){
							case 0:
								sort.setSortType(SortType.STSTEMDEFULT);
								getSubDirectory(currentPath);
								showActivity(currentPath);
								break;
							case 1:
								sort.setSortType(SortType.LASTMOTIFIERTIME);
								getSubDirectory(currentPath);
								showActivity(currentPath);
								break;
							case 2:
								sort.setSortType(SortType.NAME);
								getSubDirectory(currentPath);
								showActivity(currentPath);
								break;
							case 3:
								sort.setSortType(SortType.FILIANDDIRECTORY);
								getSubDirectory(currentPath);
								showActivity(currentPath);
								break;
							case 4:
								sort.setSortType(SortType.SIZE);
								getSubDirectory(currentPath);
								showActivity(currentPath);
								break;
							case 5:
							}
						}
					});
			dialog.create().show();
			break;

		case Menu.FIRST+3:
			Toast.makeText(this,"����ƶ��˵��������",Toast.LENGTH_SHORT).show();
			// �ж��ܷ��������ƶ�������
			if(CONSTANT_MOVE==true){
				// һ�����ĸ��ո�.
				Toast.makeText(this,"����δ�����ƶ�������",Toast.LENGTH_SHORT).show();
			}else{
				final File destDir=new File(currentPath,moveFile.getName());
				// �ж��Ƿ���Ŀ¼�ظ����ļ��ظ�����
				if(destDir.exists()){
					String message="";
					if(moveFile.isDirectory()){
						message="��Ŀ¼�Ѵ��ڣ���ȷ����Ҫ��������";
					}else if(moveFile.isFile()){
						message="���ļ��Ѵ��ڣ���ȷ����Ҫ��������";
					}
					AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
					bui.setTitle("���棺");
					bui.setMessage(message);
					bui.setIcon(android.R.drawable.ic_menu_info_details);
					bui.setPositiveButton("ȷ��",new OnClickListener()
					{
						public void onClick(DialogInterface dialog,int which){
							try{
								if(moveFile.isDirectory()){
									delDir(destDir);
									moveFilesTo(moveFile,destDir);
								}else if(moveFile.isFile()){
									// copyFileTo(moveFile,destDir);
									moveFileTo(moveFile,destDir);
								}
								CONSTANT_MOVE=true;
							}catch(IOException e){
								CONSTANT_MOVE=true;
								e.printStackTrace();
							}
							getSubDirectory(currentPath);
							showActivity(currentPath);
						}
					});
					bui.setNegativeButton("ȡ��",null);
					bui.create().show();
				}else{
					try{
						if(moveFile.isDirectory()){
							moveFilesTo(moveFile,destDir);
						}else if(moveFile.isFile()){
							moveFileTo(moveFile,destDir);
						}
						CONSTANT_MOVE=true;
					}catch(IOException e){
						CONSTANT_MOVE=true;
						e.printStackTrace();
					}
					getSubDirectory(currentPath);
					showActivity(currentPath);
				}
			}
			break;
		case Menu.FIRST+4:
			//Toast.makeText(this,"�˳��˵��������",Toast.LENGTH_SHORT).show();
			System.exit(0);
			break;
		case Menu.FIRST+5:
			//Toast.makeText(this,"�����˵��������",Toast.LENGTH_SHORT).show();
			AlertDialog helpDialog=new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher).setTitle("    ����")
					.setMessage("�ļ�������\n�汾��v1.0 ")
					.setPositiveButton("ȷ��",new OnClickListener()
					{
						public void onClick(DialogInterface dialog,int which){

						}
					}).create();
			helpDialog.show();
			break;
		}
		return false;
	}

	// ���÷��ؼ����� ������˷��ؼ� �ͷ��ظ�·��
	public boolean onKeyDown(int keyCode,KeyEvent event){
		File di=null;
		if(keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
			// do something...
			if(!npaths.equals("/")){
				di=new File(npaths);
				getSubDirectory(di);
				showActivity(di);
			}else{
				new AlertDialog.Builder(this)
						.setTitle("              ��������")
						.setMessage("ȷ����")
						.setPositiveButton("��",
								new android.content.DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog,int which){
										// TODO Auto-generated method stub
										System.exit(0);
									}
								}).setNegativeButton("��",null).show();
			}
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}

}