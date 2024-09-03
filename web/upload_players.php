<?php
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if ($data) {
    file_put_contents('players.json', $json);
    echo "Success";
} else {
    http_response_code(400);
    echo "Invalid data";
}
