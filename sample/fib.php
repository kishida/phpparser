<?php
function fib($n) {
    if ($n < 2) {
        return $n;
    }
    return fib($n - 1) + fib($n - 2);
}
$start = microtime(1);
echo "fib:".fib(31);
echo "\n";
echo "time:".(microtime(1) - $start);
echo "\n";
fib(31);fib(31);fib(31);fib(31);fib(31);
$start = microtime(1);
echo "fib:".fib(31);
echo "\n";
echo "time:".(microtime(1) - $start);
echo "\n";
