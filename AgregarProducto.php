<?php
    header('Content-type: bitmap; charset=utf-8 ');
    $con = mysqli_connect("localhost", "id1453347_microadmin", "microadmin17", "id1453347_microadmin");

    $imagenCodificada = $_POST["imagenCodificada"];
    $codigo = $_POST["codigo"];
    $nombre = $_POST["nombre"];
    $preciounidad = $_POST["preciounidad"];
    $costomanufactura = $_POST["costomanufactura"];
    $imagen = $_FILES["imagen"];
    $cantidad = $_POST["cantidad"];
    
    $idproducto;
    $uploadPath = 'img/';
    //$serverIP = gethostbyname(gethostname());
    $uploadURL = 'http://microadmin.000webhostapp.com/'.$uploadPath;
    //agregar lo del url a imagen

    function ingresarProducto() {
    global $con, $codigo, $nombre, $preciounidad, $costomanufactura, $idproducto, $imagenCodificada;
    
    $decoded_string = base64_decode($imagenCodificada);
    $path = 'http://microadmin.000webhostapp.com/'.'img/'.$codigo.'JPEG';
    
    //Guarda imagen en el server
    $file = fopen($path, 'wb');
    
    $is_written = fwrite($file,$decoded_string); 
    
    fclose($file);

    try{
        $statement = "INSERT INTO Producto (Codigo, Nombre, PrecioUnidad, CostoManufacturaUnidad, URLImagen) VALUES (?, ?, ?, ?, ?)";
        mysqli_stmt_bind_param($statement,'ssdds', $codigo, $nombre, $preciounidad, $costomanufactura, $path);
        $result = mysqli_stmt_execute($statement);

        if ($result === TRUE) {
            $idproducto = mysqli_insert_id($con);
            ingresarInventario();
        }
        mysqli_stmt_close($statement);  

    }catch(Exception $e){
        $response['error'] = false;

     }
    }

    function getFileName(){
        $con = mysqli_connect("localhost", "id1453347_microadmin", "microadmin17", "id1453347_microadmin");
        $statement = "SELECT MAX(IDProducto) AS IDProducto FROM Producto";
        $result = mysqli_fetch_array(mysqli_query($con, $statement));
        mysqli_close($con);
        if($result['IDProducto'] == null){
            return 1;
        }else{
            return ++$result['IDProducto'];
        }

    }

    function ingresarInventario(){
        global $con, $idproducto, $codigo, $cantidad;
        $statement = mysqli_prepare($con, "INSERT INTO Inventario (IDProducto, Cantidad) VALUES (?, ?)");
        mysqli_stmt_bind_param($statement,'ii', $idproducto, $cantidad);
        mysqli_stmt_execute($statement);
        mysqli_stmt_close($statement);  
    }


    ingresarProducto();
    $response = array();
    $response["success"] = true;  
?>
