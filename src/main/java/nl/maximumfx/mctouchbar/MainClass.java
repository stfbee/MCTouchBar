package nl.maximumfx.mctouchbar;

import com.thizzer.jtouchbar.JTouchBar;
import com.thizzer.jtouchbar.item.view.TouchBarButton;
import com.thizzer.jtouchbar.item.view.TouchBarTextField;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFWNativeCocoa;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainClass implements ModInitializer {

	private static MinecraftClient mcc;
	private static long window;
	private Screens active;
	private Bars bars;

	private static JTouchBar inGameTouchBar;
	private static JTouchBar mainTouchBar;

	private TouchBarButton healthBtn;
	private int oldHealth = 0;

	private TouchBarButton arrowsBtn;
	private int oldArrows = 0;

	private TouchBarTextField coords;
	private int[] oldCoords = new int[]{0, 0, 0};

	private TouchBarButton hud, screenshot, debug, shaders, cycleCamera, streamOnOff, streamPause, fullScreen;

//	private TouchBarButton hitBox;

	@Override
	public void onInitialize() {
		if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			Logger.log(Level.INFO, "Initialised MCTouchBar");
			mcc = MinecraftClient.getInstance();
			Logger.log(Level.INFO, "Waiting on window...");

			Executors.newSingleThreadScheduledExecutor().schedule(() -> {
				Helper.init(this);
				Logger.log(Level.INFO, "Loading icons...");
				Icons.init();
				Logger.log(Level.INFO, "Creating TouchBars...");
				bars = new Bars();
				bars.init(this);
				mainTouchBar = bars.getInfoBar();
				inGameTouchBar = bars.getInGameBar();
				Logger.log(Level.INFO, "TouchBar content created");
				if (mcc.getWindow() != null) {
					window = GLFWNativeCocoa.glfwGetCocoaWindow(mcc.getWindow().getHandle());
					Logger.log(Level.INFO, "Info TouchBar Shown");

					ClientTickEvents.END_CLIENT_TICK.register(client -> {
						//TODO Page specific bars
						if (active != Screens.getActive()) {
							active = Screens.getActive();
							Logger.log(Level.DEBUG, active);
							if (mcc.currentScreen == null) Logger.log(Level.INFO, "CS null");
							else if (active == null) Logger.log(Level.INFO, mcc.currentScreen.getTitle().getString());
							if (active == Screens.INGAME || active == Screens.GAME_MENU) {
								show(inGameTouchBar);
							}
							else {
								show(bars.getDebugBar());
							}
						}
					});
				}
				else Logger.log(Level.ERROR, "Can't setup TouchBar");
			}, 5, TimeUnit.SECONDS);
		}
		else Logger.log(Level.FATAL, "Can't initialize MCTouchBar. This device is not a Mac.");
	}

	void reload() {
		Logger.log(Level.INFO, "Reloading MCTouchBar...");
		Icons.reload();
		bars.reload();
		mainTouchBar = bars.getInfoBar();
		inGameTouchBar = bars.getInGameBar();
		//TODO set updated bar
		Logger.log(Level.INFO, "Reloaded MCTouchBar.");
	}

	void show(JTouchBar touchBar) {
		touchBar.show(window);
	}

	public void debugWarn(final String string, final Object... arr) {
		mcc.inGameHud.getChatHud().addMessage(Text.empty().append(Text.translatable("debug.prefix").formatted(Formatting.YELLOW, Formatting.BOLD)).append(" ").append(Text.translatable(string, arr)));
	}
}
