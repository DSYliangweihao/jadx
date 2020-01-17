package jadx.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import jadx.cli.LogHelper;
import jadx.gui.settings.JadxSettings;
import jadx.gui.settings.JadxSettingsAdapter;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.NLS;
import jadx.gui.utils.SystemInfo;
import jadx.gui.utils.logs.LogCollector;

public class JadxGUI {
	private static final Logger LOG = LoggerFactory.getLogger(JadxGUI.class);

	public static void main(String[] args) {
		try {
			// logo 打印
			LogCollector.register();
			// 设置log 的打印类型
			final JadxSettings settings = JadxSettingsAdapter.load();
			settings.setLogLevel(LogHelper.LogLevelEnum.DEBUG);
			// overwrite loaded settings by command line arguments
			if (!settings.overrideProvided(args)) {
				return;
			}
			if (!tryDefaultLookAndFeel()) {
				//设置不同的系统所对应的window class 路径  可以看做 反射实例化一个window对象
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			// 设置当前的系统语言  默认是 英文
			NLS.setLocale(settings.getLangLocale());
			LOG.debug("language " + settings.getLangLocale().get().toString());
			// 打印一下系统的信息
			printSystemInfo();
			// 窗口的入口函数  类似创建一个Handler然后放到窗口中
			Runnable runnable = new MainWindow(settings)::init;
			SwingUtilities.invokeLater(runnable);
		} catch (Exception e) {
			LOG.error("Error: {}", e.getMessage(), e);
			// 出现异常 退出应用
			System.exit(1);
		}
	}

	private static boolean tryDefaultLookAndFeel() {
		String defLaf = System.getProperty("swing.defaultlaf");
		if (defLaf != null) {
			try {
				UIManager.setLookAndFeel(defLaf);
				return true;
			} catch (Exception e) {
				LOG.error("Failed to set default laf: {}", defLaf, e);
			}
		}
		return false;
	}

	private static void printSystemInfo() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Starting jadx-gui. Version: '{}'. JVM: {} {}. OS: {} {}",
					SystemInfo.JADX_VERSION,
					SystemInfo.JAVA_VM, SystemInfo.JAVA_VER,
					SystemInfo.OS_NAME, SystemInfo.OS_VERSION);
		}
	}
}
