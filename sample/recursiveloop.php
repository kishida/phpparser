<?php
function loop($value, $count) {
    if ($count == 0) {
        return $value;
    }
    return loop($value + 2, $count - 1);
}

print loop(0, 10000000);