import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dries Meerman
 */
public class Main {
    public static void main(String[] args) {

        Date d = new Date();
        BigInteger startNumber = new BigInteger("1000000");//monolitilcly 36 ish seconds 300 000 || 90k 7 seconds
        System.out.println("Start: "+d);

        BigInteger monoResult = BigInteger.ZERO;
        BigInteger result     = BigInteger.ZERO;


        System.out.println("Start fact thread with " + startNumber + "\n" );
        if (true){
            try {
                long start = System.nanoTime();

                int starts = startNumber.intValue();
                System.out.println("startval="+starts);
                result     = threadedFactorial(starts, 20);
                long end   = System.nanoTime();
//                System.out.println("Threaded result ="+result); //Commented line because result output is too huge.
                System.out.println("Result length=" + getDigitCount(result));
                long duration = end-start;
                System.out.println("\n" + duration + " Nano seconds.\n");
                System.out.println(duration / 1000 + " Micro seconds");
                System.out.println(duration / 1000000 + " Milli seconds");
                System.out.println(duration / 1000000000 + " Seconds");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n-------------------\n\n////////////////\n//Start normal\n////////////////\n");
        monoResult = calculateFactorial(startNumber);
//        System.out.println("Monolithic result ="+monoResult); //Commented line because result output is too huge.
        System.out.println("Result length="+getDigitCount(monoResult));

        System.out.println("Comparing results if 0 its the same "+ monoResult.compareTo(result));
    }

    public static void ayy(){
        while (true) {
            System.out.println("memes");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author Dries Meerman
     * @description Divides a factorial calculation between a given amount of threads
     * @param toFactor
     * @param threadCount
     * @return
     * @throws Exception
     */
    public static BigInteger threadedFactorial(int toFactor, int threadCount) throws Exception {

        if (threadCount >= toFactor){
            throw new Exception("To many threads");
        }


        AtomicReference<BigInteger> result           = new AtomicReference<>();
        result.getAndUpdate(bigInteger -> bigInteger = new BigInteger("1"));

        ExecutorService es    = Executors.newCachedThreadPool();
        BigInteger factor     = BigInteger.valueOf(toFactor);
        BigInteger multiplier = factor.divide(BigInteger.valueOf((long)threadCount));
        BigInteger modulo     = factor.mod(BigInteger.valueOf((long)threadCount));

        for (int i = 0; i < threadCount; i++){
            final BigInteger chunkStart = factor.subtract(multiplier.multiply(BigInteger.valueOf((long)i)));

            es.execute(() -> {
                result.getAndUpdate(bigInteger -> bigInteger.multiply(threadedFactPart(chunkStart, multiplier)));
            });
        }

        if (modulo.compareTo(BigInteger.ZERO) != 0) {
            es.execute(() -> {
                result.getAndUpdate(bigInteger -> bigInteger.multiply(threadedFactPart(modulo, modulo)));
            });
        }

        es.shutdown();

        boolean finished = false;

        try {
            finished = es.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!finished){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result.get();
    }


    /**
     * @author Dries Meerman
     * @param selected
     * @param amount
     * @return returns the selected times selected-1 for amount times where selected -= 1
     */
    private static BigInteger threadedFactPart(BigInteger selected, BigInteger amount){
        BigInteger next, result;

        result         = selected;
        BigInteger max = selected.subtract(amount.subtract(BigInteger.ONE));
        max            = (max.compareTo(BigInteger.ZERO) <= 0)? new BigInteger("1"): max;

        while(selected.compareTo(max) != 0 && selected.compareTo(BigInteger.ZERO) >= 0){
            next     = selected.subtract(BigInteger.ONE);
            result   = result.multiply(next);
            selected = next;
        }
        return result;
    }


    /**
     * @author Dries Meerman
     * @description Calculates factorial monolithlicly
     * @param selected
     * @return
     */
    public static BigInteger calculateFactorial(BigInteger selected){
        BigInteger next, result;

        result         = selected;
        long startTime = System.nanoTime();

        while(selected.compareTo(BigInteger.ONE) != 0){

            next     = selected.subtract(BigInteger.ONE);
            result   = result.multiply(next);
            selected = next;
        }
        long endTime  = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("\n" + duration + " Nano seconds.\n");
        System.out.println(duration / 1000 + " Micro seconds");
        System.out.println(duration / 1000000 + " Milli seconds");
        System.out.println(duration / 1000000000 + " Seconds");
        return result;
    }


    /**
     * @source http://stackoverflow.com/questions/18828377/biginteger-count-the-number-of-decimal-digits-in-a-scalable-method
     * @param number
     * @return
     */
    public static int getDigitCount(BigInteger number) {
        double factor = Math.log(2) / Math.log(10);
        int digitCount = (int) (factor * number.bitLength() + 1);
        if (BigInteger.TEN.pow(digitCount - 1).compareTo(number) > 0) {
            return digitCount - 1;
        }
        return digitCount;
    }
}
