package com.yimo;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

import java.io.File;


class NewNameDialog extends Object{
	private Builder dialog = null;
	private Context c = null;
	private File selectedFile = null;
	private String s = null;

	public NewNameDialog(Context c, File selectedFile) {
		this.dialog = dialog;
		this.c = c;
		this.selectedFile = selectedFile;
	}

	/**
	 * 调用该方法创建一个标题为“新名称”的有编辑框的可编辑对话框，返回字符串形式的编辑内容。
	 * 
	 * @return String:存储编辑的内容
	 */
	String showNewNameDialog() {
		Builder dialog = new Builder(c);
		dialog.setTitle("新名称：");
		dialog.setIcon(R.drawable.ic_launcher);
		final EditText edit = new EditText(c);
		edit.setHint(selectedFile.getName());
		dialog.setView(edit);
		dialog.setPositiveButton("确定", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				s = edit.getText().toString();				
			}
		});
		dialog.setNegativeButton("取消", null);
		dialog.create().show();
		return s;//在事件响应之前就返回了s
	}

}