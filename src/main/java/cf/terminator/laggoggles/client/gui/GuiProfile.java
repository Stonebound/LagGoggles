package cf.terminator.laggoggles.client.gui;

import cf.terminator.laggoggles.CommonProxy;
import cf.terminator.laggoggles.client.gui.buttons.DonateButton;
import cf.terminator.laggoggles.client.gui.buttons.DownloadButton;
import cf.terminator.laggoggles.client.gui.buttons.OptionsButton;
import cf.terminator.laggoggles.client.gui.buttons.ProfileButton;
import cf.terminator.laggoggles.packet.CPacketRequestResult;
import cf.terminator.laggoggles.packet.CPacketRequestScan;
import cf.terminator.laggoggles.packet.SPacketMessage;
import cf.terminator.laggoggles.packet.SPacketScanResult;
import cf.terminator.laggoggles.packet.SPacketServerData;
import cf.terminator.laggoggles.profiler.ProfileResult;
import cf.terminator.laggoggles.profiler.ScanType;
import cf.terminator.laggoggles.util.Perms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;

import java.io.IOException;
import java.util.ArrayList;

import static cf.terminator.laggoggles.profiler.ProfileManager.LAST_PROFILE_RESULT;

public class GuiProfile extends Screen {

    private static final int BUTTON_START_PROFILE_ID = 0;
    private static final int BUTTON_SHOW_TOGGLE      = 1;
    private static final int BUTTON_ANALYZE_RESULTS  = 2;
    private static final int LABEL_ID                = 3;
    private static final int BUTTON_DONATE           = 4;
    private static final int BUTTON_OPTIONS          = 5;
    private static final int BUTTON_DOWNLOAD         = 6;

    public static String PROFILING_PLAYER = null;
    public static long PROFILE_END_TIME = 0L;

    public static String MESSAGE = null;
    public static long MESSAGE_END_TIME = 0L;


    private ProfileButton startProfile;
    private DownloadButton downloadButton;
    private Button optionsButton;
    private boolean initialized = false;
    public int seconds = Math.min(30, SPacketServerData.MAX_SECONDS);

    public GuiProfile(){
        super(new StringTextComponent("Profiling"));
    }

    public static void update(){
        if(isThisGuiOpen() == false){
            return;
        }
        Minecraft.getInstance().displayGuiScreen(new GuiProfile());
    }

    public static void open(){
        Minecraft.getInstance().displayGuiScreen(new GuiProfile());
    }

    private static boolean isThisGuiOpen(){
        return Minecraft.getInstance().currentScreen != null && (Minecraft.getInstance().currentScreen instanceof GuiProfile == true);
    }

    @Override
    public void init(){
        super.init();

        buttonList = new ArrayList<>();
        labelList = new ArrayList<>();

        int centerX = width/2;
        int centerY = height/2;

        boolean profileLoaded = LAST_PROFILE_RESULT.get() != null;

        startProfile = new ProfileButton(centerX - 100, centerY - 25, "Profile for " + seconds + " seconds");
        downloadButton = new DownloadButton(this, BUTTON_DOWNLOAD, centerX + 80, centerY - 25);
        optionsButton = new OptionsButton(BUTTON_OPTIONS, centerX - 100, centerY + 75);
        Button showToggle  = new Button(BUTTON_SHOW_TOGGLE, centerX - 100, centerY +  5, LagOverlayGui.isShowing() ? "Hide latest scan results" :
                "Show latest scan results");
        Button analyzeResults  = new Button(BUTTON_ANALYZE_RESULTS, centerX - 100, centerY +  30, "Analyze results");


        showToggle.enabled = profileLoaded;
        analyzeResults.enabled = profileLoaded;

        addButton(startProfile);
        addButton(showToggle);
        addButton(analyzeResults);
        addButton(new DonateButton(BUTTON_DONATE, centerX + 10, centerY + 75));
        addButton(optionsButton);
        Label scrollHint = new GuiLabel(fontRenderer, LABEL_ID, centerX - 100, centerY - 55, 200, 20, 0xFFFFFF);
        scrollHint.addLine("Scroll while hovering over the button");
        scrollHint.addLine("to change time time!");
        labelList.add(scrollHint);
        addButton(downloadButton);
        initialized = true;
        updateButton();
    }

    private Runnable buttonUpdateTask = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
                if(isThisGuiOpen() == false){
                    return;
                }
                updateButton();
            } catch (InterruptedException ignored){}
        }
    };

    private void updateButton(){
        if(initialized == false){
            return;
        }
        if(getSecondsLeftForMessage() >= 0){
            startProfile.setMessage(MESSAGE);
            startProfile.active = false;
            new Thread(buttonUpdateTask).start();
        }else if(getSecondsLeftForProfiler() >= 0){
            startProfile.setMessage(PROFILING_PLAYER + " > " + getSecondsLeftForProfiler() + " seconds.");
            startProfile.active = false;
            new Thread(buttonUpdateTask).start();
        }else{
            startProfile.active = true;
            startProfile.setMessage("Profile for " + seconds + " seconds");
        }
        downloadButton.active = SPacketServerData.PERMISSION.ordinal() >= Perms.Permission.GET.ordinal();
    }

    private static int getSecondsLeftForProfiler(){
        if(PROFILING_PLAYER != null) {
            return new Double(Math.ceil((PROFILE_END_TIME - System.currentTimeMillis()) / 1000)).intValue();
        }else{
            return -1;
        }
    }

    public static int getSecondsLeftForMessage(){
        return new Double(Math.ceil((MESSAGE_END_TIME - System.currentTimeMillis()) / 1000)).intValue();
    }

    @Override
    public void handleMouseInput() throws IOException{
        if(initialized == false){
            return;
        }
        if(startProfile.isMouseOver() && startProfile.active){
            int wheel = Mouse.getDWheel();
            if(wheel != 0) {
                seconds = seconds + ((wheel / 120) * 5); /* 1 Click is 120, 1 click is 5 seconds */
                seconds = Math.max(seconds, 5);
                boolean triedMore = seconds > SPacketServerData.MAX_SECONDS;
                seconds = Math.min(seconds, SPacketServerData.MAX_SECONDS);
                if(triedMore){
                    startProfile.setMessage("Limited to " + seconds + " seconds.");
                }else {
                    startProfile.setMessage("Profile for " + seconds + " seconds");
                }
            }
        }
        super.handleMouseInput();
        Mouse.getDWheel();
    }

    public void startProfile(){
        CPacketRequestScan scan = new CPacketRequestScan();
        scan.length = seconds;
        startProfile.active = false;
        startProfile.setMessage("Sending command...");
        CommonProxy.channel.sendToServer(scan);
    }

    private void analyzeResults(){
        ProfileResult result = LAST_PROFILE_RESULT.get();
        if(result != null) {
            if(result.getType() == ScanType.WORLD) {
                Minecraft.getInstance().displayGuiScreen(new GuiScanResultsWorld(result));
            }else if(result.getType() == ScanType.FPS){
                Minecraft.getInstance().displayGuiScreen(new GuiFPSResults(result));
            }
        }
    }

    @Override
    public boolean handleComponentClicked(ITextComponent gui){
        switch (gui.getString()){
            case BUTTON_START_PROFILE_ID:
                startProfile.onFPSClick(this);
                break;
            case BUTTON_SHOW_TOGGLE:
                if(LagOverlayGui.isShowing()) {
                    LagOverlayGui.hide();
                    Minecraft.getInstance().displayGuiScreen(null);
                }else{
                    LagOverlayGui.show();
                    Minecraft.getInstance().displayGuiScreen(null);
                }
                break;
            case BUTTON_ANALYZE_RESULTS:
                analyzeResults();
                break;
            case BUTTON_DONATE:
                DonateButton.donate();
                break;
            //            case BUTTON_OPTIONS:
            //                Minecraft.getInstance().displayGuiScreen(new GuiInGameConfig(this));
            //                break;
            case BUTTON_DOWNLOAD:
                CommonProxy.channel.sendToServer(new CPacketRequestResult());
                break;
        }

    }


}
