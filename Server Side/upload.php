<?php
    $imageContent = $_REQUEST['imageContent'];
    $imageExtension = $_REQUEST['imageType'];
    if (empty($imageContent)) {
        die("error:No image content specified.");    
    }
    if (empty($imageExtension)) {
        $imageExtension = "jpg";
    }
    
    $imageName = time() . "-" . rand(1000, 9999) . ".$imageExtension";
    $binary = base64_decode($imageContent);
    header('Content-Type: bitmap; charset=utf-8');
    $file = fopen("./../upload/images/$imageName", 'wb');
    fwrite($file, $binary);
    fclose($file);
    die("ok:" . $imageName);
?>