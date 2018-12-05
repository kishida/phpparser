/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kis.phpparser;

/**
 *
 * @author naoki
 */
public class Main {
    private static String SAMPLE = `
        <?php
        function fib($n) {
            if ($n < 1) {
                return 0;
            }
            return fib($n - 1) + fib($n - 2);
        }
        echo fib(10);
    `;
}
