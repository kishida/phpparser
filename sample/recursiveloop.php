<?php
function loop($value, $count) {
    if ($count < 1) {
        return $value;
    }
    return loop($value + 2, $count - 1);
}

echo loop(0, 10000000);