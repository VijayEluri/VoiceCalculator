package org.wolink.app.voicecalc;

import java.util.Calendar;

public class Utils {
	// Ϊ�˱ܹ����г�������ڣ��ڷ����������ʾ���ͻ���ǽ
	static boolean isVerifyTime() {
		final Calendar c = Calendar.getInstance();
		
		// ע���·ݵ���ֵ��ʵ���·�ҪС1.
		Calendar cc = Calendar.getInstance();
		cc.set(2011, 9, 19);
		
		if (c.after(cc)) {
			return false;
		}
		
		return true;
	}
}
