package controlmidleservernostag;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CMSNSMain {

    public static void chayvoilamdathaydoi() throws IOException{
        FileWriter fw = null; // file luu kq
        try {
            fw = new FileWriter("ket qua khi lamda thay doi mo hinh no stag.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }


        // // co dinh alpha, lamda thay doi
        double[] alpha = {0.1};//,0.5, 1, 5, 10, 20, 50, 100, 150, 200};

        int n = 20;
        double[] lamdaarray = new double[n];

        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberSetupServerNoMidle = new double[n];
        double[] meanNumberOnServerNoMidle = new double[n];

        double[] meanWaittime = new double[n];
        double[] meanNumberSetupServer = new double[n];
        double[] meanNumberOnServer = new double[n];
        double[] meanNumberMiddleServer = new double[n];
        double[] meanNumberOffToMiddleServer = new double[n];


        for (int j = 0; j<alpha.length;j++) {
            // voi moi h thuc hien cho lamda chay


            for (int i = 0; i < 1; i++) {
                lamdaarray[i] = 1;


                // chay khong co middle

                Log.printLine("(chay ko co middle) Starting simulation... chay voi lamda = "+lamdaarray[i]);
                try {
                    //------------------thiet lap tham so-----------------
                    CMSNSHelper.reset();
                    CMSNSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSNSHelper.setAlpha(alpha[j]);
                    CMSNSHelper.setMuy(0.2);


                    CMSNSHelper.setTimeOffToMiddle(200);
                    boolean hasMiddle = false;

                    CMSNSHelper.initRandom();
//                    CMSHelper.totalJobs = 4100000;

                    //------------------thiet lap tham so (end)-----------------

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                    CMSNSDatacenter datacenter0 = CMSNSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);



                    //Third step: Create Broker
                    DatacenterBroker broker = CMSNSHelper.createBroker();

                    int brokerId = broker.getId();
                    // Sixth step: Starts the simulation
                    double lastclock = CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("mean waitting time: " + ((CMSNSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSNSHelper.getCloudletid());
                    Log.printLine("total vm: " + CMSNSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSNSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }
            }



            //------------ ko co middle-------------

            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number setup server no middle = [");fw.write("mean_number_of_setup_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServerNoMidle[i]);fw.write(meanNumberSetupServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServerNoMidle[n-1]);fw.write(meanNumberSetupServerNoMidle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server no middle = [");fw.write("mean_number_of_on_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServerNoMidle[i]);fw.write(meanNumberOnServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServerNoMidle[n-1]);fw.write(meanNumberOnServerNoMidle[n-1] + "];\n");
            Log.printLine();


            Log.printLine();

            // in code ve:
            fw.write("plot(lamda,mean_waitting_time,'r',lamda,mean_waitting_time_no_middle,'-xb');\n");
            fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean waitting time');\n");
            fw.write("legend('with middle','no middle');\n");
            fw.write("ylim([0 max(max(mean_waitting_time),max(mean_waitting_time_no_middle))*10/9]);");

//            fw.write("ylim([0 18000]);\n");
            fw.write("figure(2);\n");
            fw.write("plot(lamda,mean_number_of_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number of middle server');\n");
            fw.write("ylim([0 max(mean_number_of_middle_server)*10/9]);");

            fw.write("figure(3);\n");
            fw.write("plot(lamda,mean_number_of_off_to_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number off to middle server');\n");
            fw.write("ylim([0 max(mean_number_of_off_to_middle_server)*10/9]);");

            // ------ve do thi mean number of ON server va Setup server
            fw.write("figure(4);\n");
            fw.write("plot(lamda,mean_number_of_setup_server,'r',lamda,mean_number_of_setup_server_no_middle,'-xb');\n");
            fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number setup server');\n");
            fw.write("legend('with middle','without middle');\n");
            fw.write("ylim([0 max(max(mean_number_of_setup_server),max(mean_number_of_setup_server_no_middle))*10/9]);");

            fw.write("figure(5);\n");
            fw.write("plot(lamda,mean_number_of_on_server,'r',lamda,mean_number_of_on_server_no_middle,'-xb');\n");
            fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number on server');\n");
            fw.write("legend('with middle','without middle');\n");
            fw.write("ylim([0 max(max(mean_number_of_on_server),max(mean_number_of_on_server_no_middle))*10/9]);\n");
//            fw.write("ylim([0 1000]);\n");
            Log.printLine("--------------------------------------");
            fw.write("--------------------------------------\n");
        }


        fw.close();
    }


    public static void chayvoihthaydoi()     throws IOException{
        FileWriter fw = null; // file luu kq
        try {
            fw = new FileWriter("ket qua khi h thay doi.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }


        // // co dinh h, lamda thay doi
        double[] lamdaarray = {0.1 , 0.2, 0.5, 1, 3, 5, 10, 15, 20, 30, 40};

        int n = 300;
        double[] h = new double[n];
        double[] meanWaittime = new double[n];
        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberMiddleServer = new double[n];





        for (int j = 0; j<lamdaarray.length;j++) {
            // voi moi h thuc hien cho lamda chay


            for (int i = 0; i < n; i++) {
                h[i] = (i + 1); // h chay tu 1 den 200

                // chay voi co control middle
                Log.printLine("Starting simulation... chay voi lamda = "+lamdaarray[j]+" i="+i);
                try {
                    CMSNSHelper.reset();
                    CMSNSHelper.setLamda(lamdaarray[j]);
                    CMSNSHelper.setControlTime(h[i]);

                    CMSNSHelper.setAlpha(0.0125);
                    CMSNSHelper.setMuy(0.05);
                    CMSNSHelper.setTimeOffToMiddle(200);

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
                    @SuppressWarnings("unused")
                    CMSNSDatacenter datacenter0 = CMSNSHelper.createDatacenter("Datacenter_0");
//                datacenter0.setHasMiddle(false);
                    //Third step: Create Broker
                    DatacenterBroker broker = CMSNSHelper.createBroker();
                    int brokerId = broker.getId();


                    // Sixth step: Starts the simulation
                    CloudSim.startSimulation();


                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + datacenter0.getTotalJob());
                    Log.printLine("total vm: " + CMSNSHelper.getVmid());
                    Log.printLine();

                    meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                    meanWaittime[i] = datacenter0.getMeanWaittingTime();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }



            }



            // in ket qua: voi h = h[j]
            // ket qua co the copy vao matlab de ve
            Log.printLine();
            fw.write("\n");
            Log.printLine("--------------------------------------");
            fw.write("--------------------------------------\n");
            fw.write("lamda = " + lamdaarray[j] + "\n"); fw.write("\n");
            Log.printLine("close all");fw.write("close all\n");

            Log.print("control_time = [");fw.write("control_time = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f, ", h[i]);fw.write(h[i]+", ");
            }
            System.out.printf("%.2f ];", h[n-1]);fw.write(h[n-1] + "];\n");
            Log.printLine();

            Log.print("mean waitting time = [");fw.write("mean_waitting_time = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f, ", meanWaittime[i]);fw.write(meanWaittime[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittime[n-1]);fw.write(meanWaittime[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number middle server = [");fw.write("mean_number_of_middle_server = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberMiddleServer[i]);fw.write(meanNumberMiddleServer[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberMiddleServer[n-1]);fw.write(meanNumberMiddleServer[n-1] + "];\n");
            Log.printLine();

//            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
//            for (int i = 0; i < n-1; i++) {
//                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
//            }
//            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
//            Log.printLine();



            Log.printLine();

            // in code ve:
            fw.write("plot(control_time,mean_waitting_time);\n");
            fw.write("title('lamda = " + lamdaarray[j] + "'); xlabel('control time');ylabel('mean waitting time');\n");

//            fw.write("legend('with middle','no middle');\n");
            fw.write("ylim([0 18000]);\n");
            fw.write("figure(2);\n");
            fw.write("plot(control_time,mean_number_of_middle_server); title('lamda = " + lamdaarray[j] + "');xlabel('control time');ylabel('mean number of middle server');\n");

            fw.write("ylim([0 1000]);\n");
            Log.printLine("--------------------------------------");
            fw.write("--------------------------------------\n");
        }


        fw.close();
    }

    public static void test(){

        // chay voi co control middle
        Log.printLine("Starting simulation... chay voi lamda = ");
        try {
            //------------------thiet lap tham so-----------------
            CMSNSHelper.reset();
            CMSNSHelper.setLamda(3);
//                    CMSHelper.setControlTime(200);
            CMSNSHelper.setAlpha(0.01);
            CMSNSHelper.setMuy(0.2);
            CMSNSHelper.setTimeOffToMiddle(200);
            boolean hasMiddle = true;

            double sum1 =0;
            double sum2 = 0;

            for (int k = 0; k< 1000; k++) {
                double tich1 = 1;
                for (int  l = 1; l < k +1 ; l++) {
                    tich1 = tich1 * CMSNSHelper.lamda / (CMSNSHelper.lamda + l * CMSNSHelper.alpha);
                }

                sum1 = sum1 + tich1 * k;
                sum2 = sum2 + tich1;
            }
            CMSNSHelper.controlNum = (sum1/sum2 * CMSNSHelper.alpha);
            Log.printLine("control num = " + CMSNSHelper.controlNum);

            //------------------thiet lap tham so (end)-----------------

            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = true;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

            CMSNSDatacenter datacenter0 = CMSNSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);



            //Third step: Create Broker
            DatacenterBroker broker = CMSNSHelper.createBroker();

            int brokerId = broker.getId();

            // Sixth step: Starts the simulation
            double lastclock = CloudSim.startSimulation();
            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

            Log.printLine();
            Log.printLine();

            Log.printLine("total time simulate: " + lastclock);
            Log.printLine("mean waitting time: " + ((CMSNSBroker) broker).getMeanWaittingTime());

//                    double t = 0;
//                    for(int index = 100000; index < ((CMSBroker) broker).listJob.size(); index++){
//                        t = t + ((CMSBroker) broker).listJob.get(index).getTimeStartExe()-
//                                ((CMSBroker) broker).listJob.get(index).getTimeCreate();
//                    }
//                    t = t/((CMSBroker) broker).listJob.size();
//                    Log.printLine("mean waitting time 2: "+t);
//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());

            Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

            Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
            Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

            Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

            Log.printLine("total job: " + CMSNSHelper.getCloudletid());
            Log.printLine("total vm: " + CMSNSHelper.getVmid());
            Log.printLine();


//                Log.printLine(CMSHelper.getWaittingTime(newList));

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }








    }


    public static void main(String[] args) {
//        try {
//            chayvoihthaydoi();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
        try {
            chayvoilamdathaydoi();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        test();
//        test();
//        test();
//        test();
//        test();

    }


}
