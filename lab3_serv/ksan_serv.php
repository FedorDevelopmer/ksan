<?php
$method = $_SERVER['REQUEST_METHOD'];
header("Content-type: text/plain");
echo $method;
/*    if (isset($_GET['fl_pth'])) {
        $file_path = $_GET['fl_pth'];
        $filename = 'C://WebStorage//' . $file_path;
        if (file_exists($filename)) {
            $result = '';
            $opened = fopen($filename, "r");
            $data = fread($opened, filesize($filename));
            fclose($opened);
            echo 'File WS:/' . $filename . " data: \n" . $data;
        } else {
            echo 'Error: no requesting file exist';
        }
    } else {
        echo 'Error: no file parameter';
    }
*/
switch ($method){
    case 'GET':{
        if (isset($_GET['fl_pth'])) {
            $file_path = $_GET['fl_pth'];
            $filename = 'C://WebStorage//' . $file_path;
            if (file_exists($filename)) {
                $result = '';
                $opened = fopen($filename, "r");
                $data = fread($opened, filesize($filename));
                fclose($opened);
                echo 'File WS:/' . $filename . " data: \n" . $data;
            } else {
                echo 'Error: no requesting file exist';
            }
        } else {
            echo 'Error: no file parameter';
        }
    }
        break;
    case 'PUT':{
        $putData=file_get_contents('php://input');
        if (isset($_GET['fl_pth'])) {
            $file_path = $_GET['fl_pth'];
            $filename = 'C://WebStorage//' . $file_path;
            $opened = fopen($filename, "w");
            fwrite($opened, $putData);
            fclose($opened);
            echo 'File WS:/' . $filename . " data was rewritten!\n";

        } else {
            echo 'Error: no file parameter';
        }
    }
        break;
    case 'POST':{
        $putData=file_get_contents('php://input');
        if (isset($_GET['fl_pth'])) {
            $file_path = $_GET['fl_pth'];
            $filename = 'C://WebStorage//' . $file_path;
            $opened = fopen($filename, "a+");
            fwrite($opened, $putData);
            fclose($opened);
            echo 'File WS:/' . $filename . " data was appended!\n";

        } else {
            echo 'Error: no file parameter';
        }
    }
        break;
    case 'DELETE':
        {
            if (isset($_GET['fl_pth'])) {
                $file_path = $_GET['fl_pth'];
                $filename = 'C://WebStorage//' . $file_path;
                if (file_exists($filename)) {
                    unlink($filename);
                    echo 'File WS:/' . $filename . " successfully deleted.\n";
                } else {
                    echo 'Error: no requesting file exist';
                }

            } else {
                echo 'Error: no file parameter';
            }
        }
        break;

}