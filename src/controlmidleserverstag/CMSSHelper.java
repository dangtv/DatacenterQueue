package controlmidleserverstag;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CMSSHelper {

    public static int brokerId;
    public static int datacenterId;
    public static int vmid = 0;


    public static int cloudletid = 0;
    public static int vmMips = 100000;
    public static int hostNum = 100000; // phai thoa man dieu kien  hostnum * muy >  lamda


    public static double lamda = 0.1; // thoi gian trung binh giua 2 job la 1/lamda
    public static double muy = 0.1;  // thoi gian trung binh de complete 1 jobs laf 1/muy
    public static double controlTime = 0.1; // thoi gian cho moi vong lap trong thuat toan bat server
    public static double timeOffToMiddle = 120;
    public static double alpha = 0.05; // thoi gian trung binh de bat may la 1/alpha

    public static CMSSDatacenter cmsdatacenter = null;

    public static double totalTimeSimulate = 3.5e5;
    public static double timeStartSimulate = 5e4;

    public static void setLamda(double lamda) {
        CMSSHelper.lamda = lamda;
    }

    public static void reset(){
        vmid = 0;
        cloudletid = 0;

    }

    public static int getVmid() {
        return vmid;
    }




    public static int getCloudletid() {
        return cloudletid;
    }
    public static void setMuy(double muy) {
        CMSSHelper.muy = muy;
    }

    public static void setControlTime(double controlTime) {
        CMSSHelper.controlTime = controlTime;
    }

    public static void setTimeOffToMiddle(double timeOffToMiddle) {
        CMSSHelper.timeOffToMiddle = timeOffToMiddle;
    }

    public static void setAlpha(double alpha) {
        CMSSHelper.alpha = alpha;
    }



    public static Vm getVm(){
        vmid ++;
        int mips = vmMips;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        long bw = 1000;
        int pesNumber = 1; // number of cpus
        String vmm = "Xen"; // VMM name

        Vm vm = new Vm(vmid, getBrokerId(), mips, pesNumber,
                ram, bw, size, vmm,
                new CloudletSchedulerSpaceShared());
        return vm;
    }
    public static void setBrokerId(int i) {
        brokerId = i;
    }

    public static int getBrokerId() {
        return brokerId;
    }

    public static double getLamda() {
        return lamda;
    }

    public static int getDatacenterId() {
        return datacenterId;
    }

    public static void setDatacenterId(int datacenterId) {
        CMSSHelper.datacenterId = datacenterId;
    }

    public static int getVmMips() {
        return vmMips;
    }

    public static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

                Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

//    public static double getMeanWaittingTime(List<Cloudlet> list) {
//        double waittime = 0;
//        for(int k = 0 ; k < list.size(); k++ ) {
//            waittime = waittime + list.get(k).getExecStartTime() -( (CMSJob) list.get(k)).getTimeCreate();
//        }
//        waittime = waittime / (list.size() - 0 );
//        return waittime;
//    }

//    public static double getWaittingTime(List<Cloudlet> list) {
//        int size = list.size(); double waittime = 0;
//        Cloudlet cloudlet;
//        for (int i = 0; i < size; i++) {
//            cloudlet = list.get(i);
//
//            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
//
//                waittime = waittime + cloudlet.getWaitiddngTime();
//            }
//        }
//        return waittime;
//    }

    public static CMSSDatacenter createDatacenter(String name) {
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();
        List<Host> hostListOn = new ArrayList<Host>();
        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = vmMips * 3/2;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Host with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        for (int i = 0; i< hostNum; i++) {
            hostList.add(
                    new CMSSHost(
                            i,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList)
                    )
            ); // This is our machine
        }

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        CMSSDatacenter datacenter = null;
        try {
            datacenter = new CMSSDatacenter(name, characteristics,
                    new CMSSVmAllocationPolicy(hostListOn),
                    storageList, 0,
                    alpha , muy, lamda, controlTime, timeOffToMiddle );
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDatacenterId(datacenter.getId());
//        datacenter.controlMiddleHost();
        cmsdatacenter = datacenter;
        return datacenter;
    }

    public static DatacenterBroker createBroker(){

        DatacenterBroker broker = null;
        try {
            broker = new CMSSBroker("Broker", lamda);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        setBrokerId(broker.getId());
        return broker;
    }

    public static CMSSJob createJob(int _brokerid) {

        cloudletid++;

        long length = (long)( vmMips * StdRandom.exp(muy));
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        CMSSJob cloudlet;
        cloudlet = null;
        UtilizationModel utilizationModelNull = new UtilizationModelNull();
        try {
            cloudlet = new CMSSJob(
                    cloudletid,
                    length,
                    pesNumber,
                    fileSize,
                    outputSize,
                    utilizationModel,
//                    utilizationModelNull,
                    utilizationModelNull, utilizationModelNull);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cloudlet.setUserId(_brokerid);

        return  cloudlet;
    }
    public static int controlNum = 0;
}
