<?php
$request = file_get_contents("http://" . $_POST[region] . ".op.gg/summoner/userName=" . $_POST[user],false,stream_context_create(array('http'=>array('method'=>"GET", 'header'=>"Accept-language: en\r\n"))));
if (strpos($request,'This summoner is not registered') !== false || $_POST[user] == "") {
    echo "<script type='text/javascript'>
                (function redirect(){
                    window.location = '.#dne';
                })();
            </script>";
}
else{
    $mysqli = new mysqli("localhost","root","password","recorder");

    $query = "SELECT name FROM userdata WHERE name='$_POST[user]' AND region='$_POST[region]'";
    $result = $mysqli->query($query);

    if($result->num_rows > 0){
        echo "<script type='text/javascript'>
                (function redirect(){
                    window.location = '.#exists';
                })();
            </script>";
    }
    else{
        $query = "INSERT INTO userdata (`name`,`region`) VALUES ('$_POST[user]', '$_POST[region]')";
        $statement = $mysqli->prepare($query);

        $statement->bind_param('ss', $_POST[user], $_POST[region]);

        if($statement->execute()){
            echo "<script type='text/javascript'>
                (function redirect(){
                    window.location = '.#submit';
                })();
            </script>";
        }else{
            die('Error : ('. $mysqli->errno .') '. $mysqli->error);
        }
    }
    $statement->close();
}
?>