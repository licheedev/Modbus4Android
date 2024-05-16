package com.serotonin.modbus4j;

/**
 * Modbus配置
 */
public class ModbusConfig {

	private static boolean sEnableRtuCrc = true;
	private static boolean sShowSendLog = false;
	private static boolean sShowRecvLog = false;

	/**
	 * 是否启用Rtu的Crc校验
	 * 
	 * @return
	 */
	public static boolean isEnableRtuCrc() {
		return sEnableRtuCrc;
	}

	/**
	 * 配置是否启用Rtu的Crc校验
	 * 
	 * @param enableRtuCrc
	 */
	public static void setEnableRtuCrc(boolean enableRtuCrc) {
		sEnableRtuCrc = enableRtuCrc;
	}

	/**
	 * 配置是否打印log
	 * 
	 * @param enableSendLog 是否显示发送的数据日志
	 * @param eanbleRecvLog 是否显示接收的数据日志
	 */
	public static void setEnableDataLog(boolean enableSendLog, boolean eanbleRecvLog) {
		sShowSendLog = enableSendLog;
		sShowRecvLog = eanbleRecvLog;
	}

	/**
	 * 是否显示发送的数据日志
	 * 
	 * @return
	 */
	public static boolean isEnalbeSendLog() {
		return sShowSendLog;
	}

	/**
	 * 是否显示接收的数据日志
	 * 
	 * @return
	 */
	public static boolean isEnalbeRecvLog() {
		return sShowRecvLog;
	}

}
