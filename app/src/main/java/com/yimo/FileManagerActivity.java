package com.yimo;


import android.support.v7.app.AppCompatActivity;

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

public class FileManagerActivity extends ListActivity {
        /***
         * 文件管理器，用来列出本地的所有文件
         */
        private TextView mPath;// 这个就在列表上面，显示当前文件的路径
        private List items=null;// 存放显示的名称
        private ArrayList<File> paths=null;// 存放文件的路径
        String npaths=new String("/sdcard"); // 存放路径
        private final File rootPath=new File("/mnt/sdcard");// 这个是手机文件存储系统中的根目录，实际存在于手机中。
        // private final File sdPath=Environment.getExternalStorageDirectory();
        private File currentPath=null;
        private Context context=null;

        // 判断为空文件oo夹的标志
        private boolean isEmpty=false;

        // 创建Sort类型,用于排序。
        private Sort sort=new Sort(SortType.STSTEMDEFULT);

        /*
         * 用于判断能否进行粘贴操作的标志，默认为false状态。
         * 当点击了"复制"后，CONSTANT_PASTE的值就为true。粘贴完成后，它的值就变为false。true：能进行粘贴操作。
         * false：不能进行粘贴操作。
         */
        private static boolean CONSTANT_PASTE=false;
        private File copyFile;

        /*
         * 用于判断能否进行移动的标志，默认值为true。 当点击了"移动"后，CONSTANT_MOVE变为false。移动操作完成后，它的值就变为true。
         * true:能进行移动操作。 false:不能进行移动操作。
         */
        private static boolean CONSTANT_MOVE=true;
        private File moveFile;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.file_list);
            mPath=(TextView)findViewById(R.id.mPath);
            // testEditText=(EditText)findViewById(R.id.testEdit);
            context=this;// Activity本身就是Context。

            getSubDirectory(rootPath);
            showActivity(rootPath);

            // 实现ListActivity的列表项的长按事件响应。
            getListView().setOnItemLongClickListener(
                    new AdapterView.OnItemLongClickListener()
                    {
                        // parent:发生点击事件的 AbsListView.
                        // view:AbsListView中被点击的视图.
                        // position:视图在一览中的位置（索引）.
                        // id:被点击条目的行 ID.
                        // 实现时如果需要访问与选中条目关联的数据，可以调用 getItemAtPosition(position).
                        public boolean onItemLongClick(AdapterView parent,View view,
                                                       int position,long id){
                            final File selectedFile=paths.get(position);
                            if(!selectedFile.canRead()){
                                AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
                                bui.setTitle("警告：");
                                bui.setMessage("你目前没有权限访问该目录");
                                bui.setIcon(android.R.drawable.ic_menu_info_details);
                                bui.setPositiveButton("确定",null);
                                bui.create().show();
                            }else{
                                AlertDialog.Builder dialog=new Builder(FileManagerActivity.this);
                                dialog.setTitle("你想进行的操作：");
                                dialog.setIcon(R.drawable.ic_launcher);
                                dialog.setItems(new String[]{"打开","删除","重命名","复制","移动","属性"},
                                        new OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog,int which){
                                                // 进行操作：
                                                switch(which){
                                                    case 0:
                                                        // 你点击了打开条目
                                                        // testEditText.setText("你点击了打开条目");
                                                        openFile(selectedFile);
                                                        break;
                                                    case 1:
                                                        // 你点击了删除条目
                                                        AlertDialog.Builder bui=new Builder(
                                                                FileManagerActivity.this);
                                                        bui.setTitle("警告：");
                                                        String info;
                                                        if(selectedFile.isDirectory())
                                                            info="你确定需要删除该目录吗？";
                                                        else
                                                            info="你确定需要删除该文件吗？";

                                                        bui.setMessage(info);
                                                        bui.setIcon(android.R.drawable.ic_menu_info_details);
                                                        bui.setPositiveButton("确定",new OnClickListener()
                                                        {
                                                            public void onClick(DialogInterface dialog,int which){
                                                                // 删除指定路径名的文件夹/文件
                                                                if(selectedFile.isDirectory()){
                                                                    delDir(selectedFile);
                                                                }else
                                                                    delFile(selectedFile);
                                                                getSubDirectory(currentPath);
                                                                showActivity(currentPath);
                                                            }
                                                        });
                                                        bui.setNegativeButton("取消",null);
                                                        bui.create().show();
                                                        break;
                                                    case 2:
                                                        // 你点击了重命名条目
                                                        // testEditText.setText("你点击了重命名条目");
                                                        AlertDialog.Builder newNamedialog=new Builder(context);
                                                        newNamedialog.setTitle("新名称：");
                                                        newNamedialog.setIcon(R.drawable.ic_launcher);
                                                        final EditText edit=new EditText(context);
                                                        edit.setHint(selectedFile.getName());
                                                        newNamedialog.setView(edit);
                                                        newNamedialog.setPositiveButton("确定",
                                                                new OnClickListener()
                                                                {
                                                                    public void onClick(DialogInterface dialog,
                                                                                        int which){
                                                                        String newName=edit.getText().toString();
                                                                        // testEditText.setText(newName);
                                                                        if(newName==null)
                                                                            ;
                                                                        else{
                                                                            // 判断是否有重命名现象：文件重命名仍有部分问题。
                                                                            final File file=new File(currentPath,newName);
                                                                            if(file.exists()){
                                                                                AlertDialog.Builder bui=new Builder(
                                                                                        FileManagerActivity.this);
                                                                                bui.setTitle("警告：");
                                                                                String message="";
                                                                                if(file.isFile()){
                                                                                    message="该文件已存在，你确定需要覆盖吗？";
                                                                                }else if(file.isDirectory()){
                                                                                    message="该目录已存在，你确定需要覆盖吗？";
                                                                                }
                                                                                bui.setMessage(message);
                                                                                bui.setIcon(android.R.drawable.ic_menu_info_details);
                                                                                bui.setPositiveButton("确定",
                                                                                        new OnClickListener()
                                                                                        {
                                                                                            public void onClick(
                                                                                                    DialogInterface dialog,int which){
                                                                                                // 删除指定路径名的文件夹/文件
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
                                                                                bui.setNegativeButton("取消",null);
                                                                                bui.create().show();
                                                                            }else{
                                                                                selectedFile.renameTo(file);
                                                                                getSubDirectory(currentPath);
                                                                                showActivity(currentPath);
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                        newNamedialog.setNegativeButton("取消",null);
                                                        newNamedialog.create().show();
                                                        break;
                                                    case 3:
                                                        // 你点击了复制条目
                                                        copyFile=selectedFile;
                                                        CONSTANT_PASTE=true;
                                                        break;
                                                    case 4:
                                                        // 你点击了移动条目
                                                        // testEditText.setText("你点击了移动条目");
                                                        if(CONSTANT_MOVE==true){
                                                            moveFile=selectedFile;
                                                            // delDir(selectedFile);
                                                            CONSTANT_MOVE=false;
                                                        }else{
                                                            Toast.makeText(context,"请先完成先前的移动操作！",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                        break;
                                                    case 5:
                                                        // 你点击了属性条目
                                                        // testEditText.setText("你点击了属性条目");
                                                        if(selectedFile.isFile()){
                                                            LayoutInflater inflater2=LayoutInflater.from(context);
                                                            // dialog_set_merge是一个RadioGroup
                                                            // ，含有两个选项
                                                            View dialog_view=inflater2.inflate(R.layout.listview,
                                                                    null); // 暂时不会
                                                            TextView show=(TextView)dialog_view
                                                                    .findViewById(R.id.view_dia_text);
                                                            show.setText("   名  称    : "+selectedFile.getName()
                                                                    +"\n   路  径    : "+selectedFile.getAbsolutePath()
                                                                    +"\n修改时间 : "
                                                                    +getFileLastModifiedTime(selectedFile)
                                                                    +"\n文件大小 : "
                                                                    +FormatFileSize(selectedFile.length())
                                                                    +"\n文件类型 : "+getType(selectedFile));
                                                            AlertDialog helpDialog=new AlertDialog.Builder(
                                                                    context).setIcon(R.drawable.ic_launcher)
                                                                    .setTitle("    属性").setView(dialog_view)
                                                                    .setPositiveButton("确定",new OnClickListener()
                                                                    {
                                                                        public void onClick(DialogInterface dialog,
                                                                                            int which){

                                                                        }
                                                                    }).create();
                                                            helpDialog.show();
                                                            break;
                                                        }else{
                                                            LayoutInflater inflater2=LayoutInflater.from(context);
                                                            // dialog_set_merge是一个RadioGroup
                                                            // ，含有两个选项
                                                            View dialog_view=inflater2.inflate(R.layout.listview,
                                                                    null); // 暂时不会
                                                            TextView show=(TextView)dialog_view
                                                                    .findViewById(R.id.view_dia_text);
                                                            show.setText("   名  称    : "+selectedFile.getName()
                                                                    +"\n   路  径    : "+selectedFile.getAbsolutePath()
                                                                    +"\n修改时间 : "
                                                                    +getFileLastModifiedTime(selectedFile)
                                                                    +"\n文件类型 : 文件夹");
                                                            AlertDialog helpDialog=new AlertDialog.Builder(
                                                                    context).setIcon(R.drawable.ic_launcher)
                                                                    .setTitle("    属性").setView(dialog_view) // 上边
                                                                    .setPositiveButton("知道了",new OnClickListener()
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
            // 输出：修改时间[2] 2009-08-17 10:32:38
            return formatter.format(cal.getTime());
        }

        public String FormatFileSize(long fileS){// 转换文件大小

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
                Toast.makeText(this,"未知类型，不能打开",Toast.LENGTH_SHORT).show();
            }
        }

        // 得到文件类型 通过调用getMIME 只得到前边的类型 ”/“ 后面的去掉
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
                // {后缀名， MIME类型}
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
         * 设置文件与文件夹的图标 返回类型Int 0：表示图标为文件夹类型 1:表示图标为其他类型
         */
        private int setIcons(File file,HashMap map){
            if(file.isDirectory()){
                map.put("ItemImage",R.drawable.folder);
                return 1;
            }
            // 判断文件类型
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
         * 拷贝一个文件,srcFile源文件，destFile目标文件
         *
         * @param path
         * @throws IOException
         */
        public boolean copyFileTo(File srcFile,File destFile) throws IOException {
            if(srcFile.isDirectory()||destFile.isDirectory())
                return false;// 判断是否是文件
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
         * 拷贝目录下的所有文件到指定目录
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
                return false;// 判断是否是目录
            File[] srcFiles=srcDir.listFiles();
            for(int i=0;i<srcFiles.length;i++){
                if(srcFiles[i].isFile()){
                    // 获得目标文件
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
         * 移动一个文件到指定文件
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
         * 移动目录下的所有文件到指定目录
         *
         * @param srcDir
         * @param destDir
         * @return
         * @throws IOException
         */
        public boolean moveFilesTo(File srcDir,File destDir) throws IOException{
            // 添加的代码：
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
            // 自己加的一行代码：
            srcDir.delete();
            return true;
        }

        /**
         * 删除一个文件
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
         * 删除一个目录（可以是非空目录）
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
                    delDir(file);// 递归
                }
            }
            dir.delete();
            return true;
        }

        /*** 获得文件架构的一个方法***传入一个参数，文件路径 ***/
        private void getSubDirectory(File directory){

            currentPath=directory;
            npaths=directory.getParent();
            items=new ArrayList();
            paths=new ArrayList<File>();
            File[] files=directory.listFiles();
            // 按指定排序方式排序。
            sort.sort(files);
            // judge directory-------
            if(!directory.equals(rootPath)){
                // list；的第一行加一个选项，跳转到根目录
                HashMap<String, Object> map=new HashMap<String, Object>();
                map.put("ItemTitle","go back "+rootPath.getPath()); // 文字
                map.put("ItemImage",R.drawable.ic_launcher);// 图片
                items.add(map);
                paths.add(rootPath);//

                // 跳转到上级目录
                map=new HashMap<String, Object>();
                map.put("ItemTitle"," last directory"); // 文字
                map.put("ItemImage",R.drawable.ic_launcher);// 图片
                items.add(map);
                paths.add(directory.getParentFile());
            }
            if(files.length>0){
                /** 将文件都放到list容器中 ***/
                for(int i=0;i<files.length;i++){
                    File file=files[i];
                    // addListElement(file.getName());
                    HashMap<String, Object> map=new HashMap<String, Object>();
                    // map.put("ItemTitle",file.getName()); // 文字
                    String showContent=file.getName()+"\n"
                            +String.format("%tY/%<tm/%<td",new Date(file.lastModified()));
                    if(file.isFile()){
                        showContent+="   "+FormatFileSize(file.length());
                    }
                    map.put("ItemTitle",showContent); // 文字
                    setIcons(file,map);
                    // String.format("%tY/%tm/%td",new Date(file.lastModified()));
                    // new DateHandle(file.lastModified()).getTime()
                    // map.put("ItemImage",R.drawable.ic_launcher);// 图片
                    items.add(map);
                    paths.add(file);
                }
                isEmpty=false;
            }else{
                isEmpty=true;
            }
        }

        void showActivity(File directory){
            // 将文件路径显示在listView上方。
            mPath.setText("当前路径："+directory.getPath());
            mPath.setTextSize(15f);
            // ///**写一个适配器，讲容器中的数据（文件名）添加到ListView控件中显示，其中样式为自己定义的一个xml文件***/
            // ArrayAdapter fileList=new ArrayAdapter(getApplication(),
            // R.layout.listview,items);
            // setListAdapter(fileList); //运行 filelist
            // ////使用getApplication(),方法来获得，它是代表我们的应用程序的类，使用它可以获得当前应用的主题，资源文件中的内容等
            // //r.layout.file_rows 表明在这显示 items代表显示内容
            // //生成适配器的Item和动态数组对应的元素
            SimpleAdapter adapter=new SimpleAdapter(this,items,// 数据源：List<?
                    // extends
                    // Map<String,?>>类型
                    R.layout.file_rows,// ListItem的XML布局实现
                    // //动态数组与ImageItem对应的子项
                    new String[]{"ItemTitle","ItemImage"},
                    // ImageItem的XML文件里面的一个ImageView,两个TextView ID
                    new int[]{R.id.ItemTitle,R.id.ItemImage});
            setListAdapter(adapter);
            if(this.isEmpty==true){
                Toast.makeText(this,"当前文件夹没有文件",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onListItemClick(ListView l, View v, int position, long id){
            File selectedFile=paths.get(position);
            if(selectedFile.canRead()){
                if(selectedFile.isDirectory()){
                    // ------如果是文件夹，继续调用getFIleDir()方法打开，否则弹出对话框一枚
                    getSubDirectory(selectedFile);
                    showActivity(selectedFile);
                }else{
                    openFile(selectedFile);
                }
            }else{
                AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
                bui.setTitle("警告：");
                bui.setMessage("你没有权限访问该目录。");
                bui.setIcon(android.R.drawable.ic_menu_info_details);//
                bui.setPositiveButton("确定",null);
                bui.create().show();
            }
        }

        // 创建menu，Activity调用onCreateOptionsMenu(Menu menu)方法，传一个实现Menu接口的menu对象，供使用。
        @Override
        public boolean onCreateOptionsMenu(Menu menu){
            // menu.add()里面有四个参数依次是：
            // 第一个，组别。
            // 第二个，ID。是menu识别编号，供识别menu用的。很重要。
            // 第三个，顺序。这个参数的大小决定菜单出现的先后顺序。顺序是参数由小到大，菜单从左到右，从上到下排列。一行最多三个。
            // 第四个，显示文本。
            menu.add(0,Menu.FIRST,0,"新建目录").setIcon(android.R.drawable.ic_menu_add);
            menu.add(0,Menu.FIRST+1,1,"粘贴").setIcon(android.R.drawable.ic_menu_save);
            menu.add(0,Menu.FIRST+2,2,"排序").setIcon(
                    android.R.drawable.ic_menu_sort_by_size);
            menu.add(1,Menu.FIRST+3,3,"完成移动").setIcon(
                    android.R.drawable.ic_menu_info_details);
            menu.add(1,Menu.FIRST+4,4,"退出").setIcon(
                    android.R.drawable.ic_menu_close_clear_cancel);
            menu.add(1,Menu.FIRST+5,5,"帮助").setIcon(android.R.drawable.ic_menu_help);
            // 只有返回True才会起作用。
            return true;
        }

        // 菜单项选择事件 toast只是显示一条简短等消息
        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            switch(item.getItemId()){
                case Menu.FIRST:
                    // Toast.makeText(this, "新建目录菜单被点击了", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder newDialog=new Builder(FileManagerActivity.this);
                    newDialog.setMessage("新建目录名称");
                    newDialog.setIcon(R.drawable.ic_launcher);
                    final EditText edit=new EditText(this);
                    edit.setHint("新建目录");
                    newDialog.setView(edit);
                    newDialog.setPositiveButton("确定",new OnClickListener()
                    {
                        public void onClick(DialogInterface dialog,int which){
                            if(currentPath.canWrite()){
                                // 添加新建目录
                                String directoryName=edit.getText().toString();
                                File file=new File(currentPath,directoryName);
                                if(file.exists()){
                                    AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
                                    bui.setTitle("警告：");
                                    bui.setMessage("该目录已存在，不允许重复创建。");
                                    bui.setIcon(android.R.drawable.ic_menu_info_details);
                                    bui.setPositiveButton("确定",null);
                                    bui.create().show();
                                }else{
                                    // 创建的目录位于列表的的第一行，除了最上面的那两个返回键.
                                    file.mkdir();
                                    getSubDirectory(currentPath);
                                    showActivity(currentPath);
                                    paths.add(currentPath);
                                }
                            }else{
                                AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
                                bui.setTitle("警告：");
                                bui.setMessage("你目前没有权限进行新建文件夹操作。");
                                bui.setIcon(android.R.drawable.ic_menu_info_details);
                                bui.setPositiveButton("确定",null);
                                bui.create().show();
                            }
                        }
                    });
                    newDialog.setNegativeButton("取消",null);
                    newDialog.create().show();
                    break;
                case Menu.FIRST+1:
                    // 判断能否进行粘贴操作。
                    if(CONSTANT_PASTE==false){
                        // 一个字四个空格.
                        Toast.makeText(this,"剪贴板为空，不能粘贴\n    请先进行复制操作",Toast.LENGTH_SHORT)
                                .show();
                    }else{
                        final File destDir=new File(currentPath,copyFile.getName());
                        final boolean isDirectory=copyFile.isDirectory();
                        String message="";
                        if(isDirectory){
                            message="该目录已存在，你确定需要覆盖它吗？";
                        }else{
                            message="该文件已存在，你确定需要覆盖它吗？";
                        }
                        // 判断是否有目录重复或文件重复现象：
                        if(destDir.exists()){
                            AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
                            bui.setTitle("警告：");
                            bui.setMessage(message);
                            bui.setIcon(android.R.drawable.ic_menu_info_details);
                            bui.setPositiveButton("确定",new OnClickListener()
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
                            bui.setNegativeButton("取消",null);
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
                    //	Toast.makeText(this,"排序菜单被点击了",Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dialog=new Builder(FileManagerActivity.this);
                    dialog.setTitle("你想进行的操作：");
                    dialog.setIcon(R.drawable.ic_launcher);
                    dialog.setItems(new String[]{"系统默认","最近修改时间","名称","文件夹与文件","文件大小","其他"},
                            new OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,int which){
                                    // 进行操作：which下标从0开始
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
                    Toast.makeText(this,"完成移动菜单被点击了",Toast.LENGTH_SHORT).show();
                    // 判断能否进行完成移动操作。
                    if(CONSTANT_MOVE==true){
                        // 一个字四个空格.
                        Toast.makeText(this,"你尚未进行移动操作！",Toast.LENGTH_SHORT).show();
                    }else{
                        final File destDir=new File(currentPath,moveFile.getName());
                        // 判断是否有目录重复和文件重复现象：
                        if(destDir.exists()){
                            String message="";
                            if(moveFile.isDirectory()){
                                message="该目录已存在，你确定需要覆盖它吗？";
                            }else if(moveFile.isFile()){
                                message="该文件已存在，你确定需要覆盖它吗？";
                            }
                            AlertDialog.Builder bui=new Builder(FileManagerActivity.this);
                            bui.setTitle("警告：");
                            bui.setMessage(message);
                            bui.setIcon(android.R.drawable.ic_menu_info_details);
                            bui.setPositiveButton("确定",new OnClickListener()
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
                            bui.setNegativeButton("取消",null);
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
                    //Toast.makeText(this,"退出菜单被点击了",Toast.LENGTH_SHORT).show();
                    System.exit(0);
                    break;
                case Menu.FIRST+5:
                    //Toast.makeText(this,"帮助菜单被点击了",Toast.LENGTH_SHORT).show();
                    AlertDialog helpDialog=new AlertDialog.Builder(this)
                            .setIcon(R.drawable.ic_launcher).setTitle("    关于")
                            .setMessage("文件管理器\n版本：v1.0 ")
                            .setPositiveButton("确定",new OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,int which){

                                }
                            }).create();
                    helpDialog.show();
                    break;
            }
            return false;
        }

        // 设置返回键监听 如果按了返回键 就返回父路径
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
                            .setTitle("              结束任务")
                            .setMessage("确定吗？")
                            .setPositiveButton("是",
                                    new android.content.DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which){
                                            // TODO Auto-generated method stub
                                            System.exit(0);
                                        }
                                    }).setNegativeButton("否",null).show();
                }
                return true;
            }
            return super.onKeyDown(keyCode,event);
        }

    }