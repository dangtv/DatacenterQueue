package cmsfinitequeuestag;



import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CFQSMain {

    public static void chayvoilamdathaydoi() throws IOException{
        FileWriter fw = null; // file luu kq
        try {
            fw = new FileWriter("ket qua khi lamda thay doi mo hinh CFQ stag.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }


        // // co dinh alpha, lamda thay doi
        double[] alpha = {0.01};//,0.5, 1, 5, 10, 20, 50, 100, 150, 200};

        int n = 5;
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


            for (int i = 0; i < n; i++) {
                lamdaarray[i] = 2 * (i + 1);

                // chay co middle
                // chay voi co control middle
                Log.printLine("(with middle) Starting simulation... chay voi alpha = "+alpha[j] + " lamda = " + lamdaarray[i]);
                try {
                    //------------------thiet lap tham so-----------------
                    CFQSHelper.reset();
                    CFQSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CFQSHelper.setAlpha(alpha[j]);
                    CFQSHelper.setMuy(0.2);


                    CFQSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = true;

                    calculatetimenext();
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

                    CFQSDatacenter datacenter0 = CFQSHelper.createDatacenter("Datacenter_0");
                    datacenter0.state = CFQSDatacenter.ON;
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CFQSHelper.createBroker();


                    int brokerId = broker.getId();

                    // Sixth step: Starts the simulation
                    double lastclock =CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("(with middle) mean waitting time: " + ((CFQSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CFQSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CFQSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CFQSHelper.getVmid());
                    Log.printLine("total sub datacenter create: "+ CFQSHelper.listSubDatacenter.size());
                    Log.printLine();


                    meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                    meanNumberOnServer[i] = datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServer[i] = datacenter0.getMeanNumberSetupServer();
                    meanNumberOffToMiddleServer[i] = CFQSHelper.lamda * CFQSHelper.alpha *
                            CFQSHelper.timeOffToMiddle / (CFQSHelper.lamda + CFQSHelper.alpha);

                    meanWaittime[i] = ((CFQSBroker) broker).getMeanWaittingTime();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
                /*
                // chay khong co middle

                Log.printLine("(ko co middle) Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CMSHelper.reset();
                    CMSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSHelper.setAlpha(alpha[j]);
                    CMSHelper.setMuy(0.2);


                    CMSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = false;
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

                    CMSDatacenter datacenter0 = CMSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CMSHelper.createBroker();

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
                    Log.printLine("(without middle) mean waitting time: " + ((CMSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CMSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CMSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
            */
            }



            // in ket qua: voi h = h[j]
            // ket qua co the copy vao matlab de ve
            Log.printLine();
            fw.write("\n");
            Log.printLine("--------------------------------------");
            fw.write("--------------------------------------\n");
            fw.write("alpha = " + alpha[j] + "\n"); fw.write("\n");
            Log.printLine("close all");fw.write("close all\n");

            Log.print("lamda = [");fw.write("lamda = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f, ", lamdaarray[i]);fw.write(lamdaarray[i]+", ");
            }
            System.out.printf("%.2f ];", lamdaarray[n-1]);fw.write(lamdaarray[n-1] + "];\n");
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

            Log.print("mean number setup server = [");fw.write("mean_number_of_setup_server = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServer[i]);fw.write(meanNumberSetupServer[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServer[n-1]);fw.write(meanNumberSetupServer[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server = [");fw.write("mean_number_of_on_server = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServer[i]);fw.write(meanNumberOnServer[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServer[n-1]);fw.write(meanNumberOnServer[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number Off to Middle server = [");fw.write("mean_number_of_off_to_middle_server = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOffToMiddleServer[i]);fw.write(meanNumberOffToMiddleServer[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOffToMiddleServer[n-1]);fw.write(meanNumberOffToMiddleServer[n-1] + "];\n");
            Log.printLine();

            //------------ ko co middle-------------
/*
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
*/


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


    public static void calculatetimenext(){
        //***************************************
        // thiet lap timenext trong thuat toan control middle

        // dat pi_00 = a;
        // bieu dien tat ca pi_ij theo a
        // bat dau:
        // hang 0:

        // mang luu gia tri limiting probability hang dang xet
        // cu dich den hang tiep theo thi mang lai giam di phan tu dau tien
        double[] hangtruoc = new double[CFQSHelper.jobsqueuecapacity+1];
        // he so
        double[] a = new double[CFQSHelper.jobsqueuecapacity+1];
        double[] b = new double[CFQSHelper.jobsqueuecapacity+1];
        // xet dong 0
        double sum = 0;
        hangtruoc[0] = 1;
        sum = sum + hangtruoc[0];
        // tong cac pi_ii:
        double sum2 = hangtruoc[0];
        for(int i=1; i< CFQSHelper.jobsqueuecapacity;i++) {
            hangtruoc[i] = hangtruoc[i-1] * CFQSHelper.lamda/(CFQSHelper.lamda + CFQSHelper.alpha);
            sum = sum + hangtruoc[i];
        }
        hangtruoc[CFQSHelper.jobsqueuecapacity] =hangtruoc[CFQSHelper.jobsqueuecapacity-1] * CFQSHelper.lamda/(CFQSHelper.alpha);
        sum = sum + hangtruoc[CFQSHelper.jobsqueuecapacity];

        // xet tu dong thu 1 tro di
        // pi_11:
        hangtruoc[1] = CFQSHelper.lamda/ CFQSHelper.muy;
        sum = sum + hangtruoc[1];
        sum2 = sum2 + hangtruoc[1];
        // tinh he so:

        for(int i=1; i< CFQSHelper.jobsqueuecapacity;i++) { // i tu 1 den K-1
            // xet dong i:
            // tinh cac he so
            a[CFQSHelper.jobsqueuecapacity] = CFQSHelper.alpha *
                    hangtruoc[CFQSHelper.jobsqueuecapacity] / (CFQSHelper.alpha + i * CFQSHelper.muy);
            b[CFQSHelper.jobsqueuecapacity] = CFQSHelper.lamda / (CFQSHelper.alpha + i * CFQSHelper.muy);

            for(int j = CFQSHelper.jobsqueuecapacity -1; j > i; j--) {
                a[j] = (i * CFQSHelper.muy * a [j+1] + CFQSHelper.alpha * hangtruoc[j])
                        / ( CFQSHelper.lamda + CFQSHelper.alpha + i* CFQSHelper.muy - i * CFQSHelper.muy * b[j+1]);
                b[j] = CFQSHelper.lamda / ( CFQSHelper.lamda + CFQSHelper.alpha + i* CFQSHelper.muy - i * CFQSHelper.muy * b[j+1]);
            }
            // tinh limiting probability cho dong thu i ke tu pi_i,i+1
            double temp = 0;
            for(int j=i+1; j < CFQSHelper.jobsqueuecapacity+1;j++) {
                hangtruoc[j] = a[j]+b[j]*hangtruoc[j-1];
                temp = temp + CFQSHelper.alpha * hangtruoc[j];
                sum = sum + hangtruoc[j];
            }

            // tinh limiting probability pi_i+1,i+1
            hangtruoc [i+1] = temp / ((i+1) * CFQSHelper.muy);
            sum = sum + hangtruoc[i+1];
            sum2 = sum2 + hangtruoc[i+1];

        }

        // gia tri cua pi_00 = 1/sum
        //  => tong cac pi_ii = sum2/sum
        // tinh timenext:
        CFQSHelper.timenext = 1/(CFQSHelper.alpha*(1-sum2/sum));
        System.out.println("time next: "+ CFQSHelper.timenext);
    }


    public static void test(){

                // chay voi co control middle
                Log.printLine("Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CFQSHelper.reset();
                    CFQSHelper.setLamda(3);
//                    CMSHelper.setControlTime(200);
                    CFQSHelper.setAlpha(0.01);
                    CFQSHelper.setMuy(0.2);
                    CFQSHelper.setTimeOffToMiddle(200);
                    boolean hasMiddle = true;

//                    CMSHelper.totalJobs = 100100000;

                    calculatetimenext();
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

                    CFQSDatacenter datacenter0 = CFQSHelper.createDatacenter("Datacenter_0");
                    datacenter0.state = CFQSDatacenter.ON;
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CFQSHelper.createBroker();

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
                    Log.printLine("mean waitting time: " + ((CFQSBroker) broker).getMeanWaittingTime());

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

                    Log.printLine("total job: " + CFQSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CFQSBroker) broker).getNumberOfJob());

                    Log.printLine("total vm: " + CFQSHelper.getVmid());
                    Log.printLine("total sub datacenter create: "+ CFQSHelper.listSubDatacenter.size());
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
