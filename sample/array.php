<?php
$a = array(1, 2, 3);
echo implode(",",  $a) ."\n";

$a[] = 4;
echo implode(",",  $a) ."\n";

$a[5] = 5;
echo implode(",",  $a) ."\n";
echo "a[4]=".$a[4]."\n";

