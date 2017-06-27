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
	 * ���ø÷�������һ������Ϊ�������ơ����б༭��Ŀɱ༭�Ի��򣬷����ַ�����ʽ�ı༭���ݡ�
	 * 
	 * @return String:�洢�༭������
	 */
	String showNewNameDialog() {
		Builder dialog = new Builder(c);
		dialog.setTitle("�����ƣ�");
		dialog.setIcon(R.drawable.ic_launcher);
		final EditText edit = new EditText(c);
		edit.setHint(selectedFile.getName());
		dialog.setView(edit);
		dialog.setPositiveButton("ȷ��", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				s = edit.getText().toString();				
			}
		});
		dialog.setNegativeButton("ȡ��", null);
		dialog.create().show();
		return s;//���¼���Ӧ֮ǰ�ͷ�����s
	}

}