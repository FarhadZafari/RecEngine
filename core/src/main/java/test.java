
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.ArrayList;
import net.librec.math.structure.SparseMatrix;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fzafari
 */
public class test extends Thread {

    public static int totalNumThreads = 1;
    public static int arraySize = 1000;
    public int threadNum = 0;
    public double value;
    public Double[] A;
    public Double[] B;

    public test(int t, Double[] a, Double[] b) {
        threadNum = t;
        A = a;
        B = b;
    }

    public void run() {

        value = 0;
        for (int i = threadNum * (arraySize / totalNumThreads); i < (threadNum + 1) * (arraySize / totalNumThreads); i++) {
            value += A[i] * B[i];
        }
        this.stop();
    }

    public static void main(String[] args) {

        Double[] a = new Double[arraySize];
        Double[] b = new Double[arraySize];

        for (int i = 0; i < arraySize; i++) {
            a[i] = 1.0;
            b[i] = 2.0;
        }

        long start = System.currentTimeMillis();
        test[] t = new test[totalNumThreads];
        for (int i = 0; i < totalNumThreads; i++) {
            t[i] = new test(i, a, b);
            t[i].start();
        }
        for (int i = 0; i < totalNumThreads; i++) {
            while (t[i].isAlive());
            System.out.println(i + " finished!");
        }

        double res = 0;
        for (int i = 0; i < totalNumThreads; i++) {
            res += t[i].value;
        }

        System.out.println(res);
        System.out.println(System.currentTimeMillis() - start);

        
        
        start = System.currentTimeMillis();
        res = 0;
        for (int i = 0; i < arraySize; i++) {
            res += a[i] * b[i];
        }
        System.out.println(res);
        System.out.println(System.currentTimeMillis() - start);
        
    }
}
