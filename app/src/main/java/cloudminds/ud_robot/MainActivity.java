package cloudminds.ud_robot;

import android.os.Bundle;
import android.ros.org.roslib.ChargeTalker;
import android.ros.org.roslib.Constant;
import android.ros.org.roslib.DynamicMonitorTalker;
import android.ros.org.roslib.GoTalker;
import android.ros.org.roslib.InitPoseTalker;
import android.ros.org.roslib.ModeTalker;
import android.ros.org.roslib.MultiSettingTalker;
import android.ros.org.roslib.ShutDownTalker;

import android.ros.org.roslib.Reader;

import org.ros.android.RosActivity;
import org.ros.message.MessageListener;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

import geometry_msgs.PoseWithCovarianceStamped;
import nav_msgs.Odometry;
import std_msgs.Bool;
import std_msgs.Float64;
import std_msgs.Int64;
import std_msgs.UInt16;
import std_msgs.String;

public class MainActivity extends RosActivity {

    protected GoTalker goTalker;
    protected ChargeTalker chargeTalker;
    protected ModeTalker switchMode;
    protected DynamicMonitorTalker swjCommuSwitch;
    protected MultiSettingTalker local_info;
    protected ShutDownTalker shutdownTalker;
    protected Reader<Bool> arriveListener;
    protected Reader<Int64> errorCode;
    protected Reader<Float64> battery;
    protected Reader<Bool> zwj_standby;
    protected Reader<String> fatalError;
    protected Reader<String> swjCommunication;
    protected Reader<Bool> emergency_switch;
    protected Reader<UInt16> bumper_state;
    protected Reader<Bool> charge_state;
    protected Reader<PoseWithCovarianceStamped> currentPose;
    protected Reader<String> zigbee_status;
    protected Reader<Odometry> speed;
    protected InitPoseTalker initPoseTalker;
    protected robot_config Config;





    public MainActivity() {
        super("ExRobot", "ExRobot", URI.create(Constant.ZWJ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNodeData();



    }
    public  class  robot_config{
        double currentx;
        double currenty;
    }

    //启动ros节点
    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(chargeTalker,nodeConfiguration);
        nodeMainExecutor.execute(goTalker, nodeConfiguration);
        nodeMainExecutor.execute(switchMode, nodeConfiguration);
        nodeMainExecutor.execute(local_info, nodeConfiguration);
        nodeMainExecutor.execute(shutdownTalker,nodeConfiguration);
        nodeMainExecutor.execute(swjCommuSwitch, nodeConfiguration);
        nodeMainExecutor.execute(arriveListener, nodeConfiguration);
        nodeMainExecutor.execute(errorCode, nodeConfiguration);
        nodeMainExecutor.execute(battery, nodeConfiguration);
        nodeMainExecutor.execute(zwj_standby, nodeConfiguration);
        nodeMainExecutor.execute(fatalError, nodeConfiguration);
        nodeMainExecutor.execute(charge_state,nodeConfiguration);
        nodeMainExecutor.execute(bumper_state,nodeConfiguration);
        nodeMainExecutor.execute(emergency_switch,nodeConfiguration);
        nodeMainExecutor.execute(swjCommunication,nodeConfiguration);
        nodeMainExecutor.execute(zigbee_status,nodeConfiguration);
        nodeMainExecutor.execute(currentPose,nodeConfiguration);
        afterRosInit();
    }

    //初始化节点数据

    private void initNodeData(){



        goTalker = new GoTalker();
        chargeTalker=new ChargeTalker();
        swjCommuSwitch=new DynamicMonitorTalker();
        switchMode = new ModeTalker();
        local_info=new MultiSettingTalker();
        shutdownTalker=new ShutDownTalker();
        initPoseTalker=new InitPoseTalker();
        arriveListener = new Reader<>(Constant.ARRIVESTATE,Bool._TYPE);
        errorCode = new Reader(Constant.BLOCKSTATE,Int64._TYPE);
        battery = new Reader<>(Constant.BATTERY,Float64._TYPE);
        zwj_standby = new Reader<>(Constant.STANDBY,Bool._TYPE);
        fatalError=new Reader<>(Constant.FATALERROR,String._TYPE);
        swjCommunication=new Reader<>(Constant.DYNAMICSTATE,String._TYPE);
        emergency_switch = new Reader<>(Constant.EMERGENCYSTATE,Bool._TYPE);
        bumper_state=new Reader<>(Constant.BUMPERSTATE,UInt16._TYPE);
        charge_state =new Reader<>(Constant.CHARGESTATE,Bool._TYPE);
        currentPose=new Reader<>(Constant.CURRENTPOSE,PoseWithCovarianceStamped._TYPE);
        zigbee_status=new Reader<>(Constant.ZIGBEE,String._TYPE);
        speed=new Reader<>(Constant.SPEED,Odometry._TYPE);
        //Config=new config;
        Config= new robot_config();

        arriveListener.setMessageListener(new MessageListener<Bool>() {
            @Override
            public void onNewMessage(Bool bool) {
                if (bool.getData()) {
                    System.out.println("receive arrive msg");
                    if (arriveHandler != null)
                        arriveHandler.hanldArrive();
                }
            }
        });

        errorCode.setMessageListener(new MessageListener<Int64>() {
            @Override
            public void onNewMessage(Int64 int64) {
                int code = (int) int64.getData();

                if (code == 3 && blockHandler != null) {
                    blockHandler.hanldBlock();
                }
            }
        });

        battery.setMessageListener(new MessageListener<Float64>() {
            @Override
            public void onNewMessage(Float64 float64) {
                handleBattery(float64.getData());
            }
        });

        fatalError.setMessageListener(new MessageListener<String>() {
            @Override
            public void onNewMessage(String string) {
                if(string.getData().contains("died")){
                    handleFatalError(string.getData());
                }

            }
        });
        swjCommunication.setMessageListener(new MessageListener<String>() {
            @Override
            public void onNewMessage(String string) {
                if(string.getData().contains("0")||string.getData().contains("1")&&swjcommuHandler!=null){
                    //进入
                    swjcommuHandler.handleSwjCommu();
                }


            }
        });
        emergency_switch.setMessageListener(new MessageListener<Bool>() {
            @Override
            public void onNewMessage(Bool bool) {
                if(bool.getData()){
                    //紧急开关被按下
                    handleEmergency(bool.getData());
                }
                else{
                    handleEmergency(bool.getData());
                }
            }
        });
        bumper_state.setMessageListener(new MessageListener<UInt16>() {
            @Override
            public void onNewMessage(UInt16 uInt16) {
                if(uInt16.getData()!=0){
                    //被按下
                    handleBumper(true);
                }
                else{
                    handleBumper(false);
                }
            }
        });

        charge_state.setMessageListener(new MessageListener<Bool>() {
            @Override
            public void onNewMessage(Bool bool) {
                if(bool.getData()){
                    //充电中
                    handleCharge(true);
                }
            }
        });

        zwj_standby.setMessageListener(new MessageListener<Bool>() {
            @Override
            public void onNewMessage(Bool bool) {
                if (bool.getData()) {
                    handleStandby();
                }
            }
        });

        currentPose.setMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
            @Override
            public void onNewMessage(PoseWithCovarianceStamped pose) {
                Config.currentx = pose.getPose().getPose().getPosition().getX();
                Config.currenty = pose.getPose().getPose().getPosition().getY();



            }
        });

        zigbee_status.setMessageListener(new MessageListener<String>() {
            @Override
            public void onNewMessage(String string) {
                if(string.getData().toString()!=null){
                    handleZigbee(true);
                }

            }
        });

        speed.setMessageListener(new MessageListener<Odometry>() {
            @Override
            public void onNewMessage(Odometry odometry) {
                double LinearX=odometry.getTwist().getTwist().getLinear().getX();
                double AngularZ=odometry.getTwist().getTwist().getAngular().getZ();
            }
        });
    }




    protected void handleStandby() {
    }

    protected void afterRosInit() {

    }

    protected void handleBattery(double battery) {

    }
    protected void handleFatalError(java.lang.String errormsg) {

    }

    protected void handleEmergency(boolean flag) {
    }

    protected void handleBumper(boolean flag) {
    }

    protected void handleCharge(boolean flag){

    }

    protected void handleZigbee(boolean flag){

    }

    private ArriveHandler arriveHandler;

    protected void setArriveHandler(ArriveHandler arriveHandler) {
        this.arriveHandler = arriveHandler;
    }

    protected interface ArriveHandler {
        void hanldArrive();
    }


    private SwjCommuHandler swjcommuHandler;

    protected void setSwjCommuHandler(SwjCommuHandler swjcommuHandler) {
        this.swjcommuHandler = swjcommuHandler;
    }

    protected interface SwjCommuHandler {
        void handleSwjCommu();
    }

    private BlockHandler blockHandler;

    protected void setBlockHandler(BlockHandler blockHandler) {
        this.blockHandler = blockHandler;
    }

    protected interface BlockHandler {
        void hanldBlock();
    }
}
