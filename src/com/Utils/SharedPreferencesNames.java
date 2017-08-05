package com.Utils;

public class SharedPreferencesNames {
	
	public enum SPNames {
		UserInfo("userinfo"), LocalRecordHistory("localrecordhistory"), LocalCaptureHistory("localcapturehistory"), FunDevices("mfundevices"), WarnInfo("warninfo");

		private String value;

		private SPNames(String value) {
			this.value = value;
		};

		public String getValue() {
			return this.value;
		}
	}
	
	public enum WarnInfoItems {
		WarnInfo("warninfo");

		private String value;

		private WarnInfoItems(String value) {
			this.value = value;
		};

		public String getValue() {
			return this.value;
		}
	}
	
	public enum FunDevicesItems {
		FunDevices("mfundevices");

		private String value;

		private FunDevicesItems(String value) {
			this.value = value;
		};

		public String getValue() {
			return this.value;
		}
	}

	public enum UserInfoItems {
		hasES("hasES"), localPath("localpath");

		private String value;

		private UserInfoItems(String value) {
			this.value = value;
		};

		public String getValue() {
			return this.value;
		}
	}

	public enum LocalRecordHistoryItems {
		LocalHistory("localhistory");

		private String value;

		private LocalRecordHistoryItems(String value) {
			this.value = value;
		};

		public String getValue() {
			return this.value;
		}
	}

	public enum LocalCaptureHistoryItems {
		LocalCapture("localcapture");

		private String value;

		private LocalCaptureHistoryItems(String value) {
			this.value = value;
		};

		public String getValue() {
			return this.value;
		}
	}
}
