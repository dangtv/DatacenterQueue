javac -d bin -sourcepath src -cp libs/cloudsim-3.0.3.jar src/cmsfinitequeuenostag/CFQNSMain.java;

java -cp bin:libs/cloudsim-3.0.3.jar cmsfinitequeuenostag.CFQNSMain 0.2 0.002 50 0.2 -0.8;
java -cp bin:libs/cloudsim-3.0.3.jar cmsfinitequeuenostag.CFQNSMain 0.2 0.002 100 0.2 -0.8;
java -cp bin:libs/cloudsim-3.0.3.jar cmsfinitequeuenostag.CFQNSMain 0.2 0.002 200 0.2 -0.8;
