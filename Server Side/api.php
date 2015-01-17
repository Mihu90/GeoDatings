<?php
require_once("Rest.inc.php");
    
class API extends REST {          
    public $data = "";
    
    const DB_SERVER = "localhost";  // Server Name
   	const DB_USER = "geodatin_usrdb";        // Database User Name
   	const DB_PASSWORD = "b%rg4d1pE}H*";        // Database Password
   	const DB = "geodatin_db";         // Database Name
    
    /* Regula generica pentru selectarea unei intrari dintr-o tabela MySQL. */
    const SQL_SELECT_GENERIC = "SELECT * FROM %s WHERE %s = ?";
    /* Regula generica pentru stergerea unei intrari dintr-o tabela MySQL. */
    const SQL_DELETE_GENERIC = "DELETE FROM %s WHERE %s = ?";
    /* Interogare MySQL ce intoarce numarul de intrari selectate fara regula LIMIT */
    const SQL_SELECT_NUM_ROWS = "SELECT FOUND_ROWS() AS count"; // >= MySQL 4.0.0
    /* Tabela din baza de date care stocheaza clientii GCM */
    const SQL_TABLE_GCM_USERS = "gcm_users";
        
    public function __construct() {
        // Initializeaza constructorul parintelui.
        parent::__construct();
        // Initializeaza conexiunea cu baza de date.
        $this->dbConnect();
   	}
    
    /*
     * Realizeaza conexiunea cu baza de date.
     */
    private function dbConnect() {
  		$this->db = new mysqli(self::DB_SERVER, self::DB_USER, self::DB_PASSWORD);
  		$this->db->set_charset('utf8');
  		if ($this->db) {
 			$this->db->select_db(self::DB);
        }
   	}
    
    /*
     * Metoda folosita pentru a actualiza o intrare in baza de date.
     */
    private function updateQuery($table, $where, $update, $sign = "=") {
        $toUpdate = array();
        if (is_array($update)) {
            foreach ($update as $key => $value) {
                if (is_string($value)) {
                    $toUpdate[] = $key . "='" . $value . "'";   
                } else if (is_numeric($value)) {
                    $toUpdate[] = $key . $sign . $value;
                }
            }
        } else {
            $toUpdate[] = $update;
        }
        
        $query = "UPDATE $table SET " . @implode(",", $toUpdate) . " WHERE $where"; 
        return $this->db->query($query);
    }
    
    /*
     * Proceseaza cererea venita din partea unui client si apeleaza metoda
     * ceruta de acesta. Orice metoda apelata din afara va trebui sa fie
     * prefixata tu "api_". Asta este o regula de securitate.
     */	
   	public function process_api() {
  		$function = strtolower(trim(str_replace("/", "", $_REQUEST['request'])));
  		if (strpos($function, "api_") === 0 && (int)method_exists($this, $function) > 0) {
  		    if ($this->get_request_method() != "GET") {
    			$this->response('', 406);
            } else {
 			    $this->$function();
            }
  		} else {
 			$this->response('', 404);
        }
   	}
    
    /*
     * Metoda este folosita pentru a inregistra un nou device Android pentru
     * a primi notificari folosind serviciul Google Cloud Messaging.
     */
    private function api_register_id()
    {
        $registerID = $_GET['registerID'];
        $deviceID = $_GET['deviceID'];
            
        // Exista deja o intrare in baza de date?
        $query = sprintf(self::SQL_SELECT_GENERIC, self::SQL_TABLE_GCM_USERS, "deviceID");
        
        $countRows = 0;
        $stmt = $this->db->stmt_init();
	    if ($stmt->prepare($query)) {
            $stmt->bind_param('s', $deviceID);
            $stmt->execute();
            $stmt->store_result();
            $countRows = $stmt->num_rows;
            $stmt->close();
        }
        
        // Daca intrarea deja exista atunci se va actualiza
        if ($countRows > 0) {
            $update = array(
                'registerID' => $register_id,
                'timestamp' => time()
            );
            $result = $this->updateQuery(self::SQL_TABLE_GCM_USERS,
                                         "deviceID = '$deviceID'",
                                         $update);
        } else {
            $query = "INSERT INTO " . self::SQL_TABLE_GCM_USERS . "
                      VALUES(NULL, '$registerID', '$deviceID', '" . time() . "')";
            $result = $this->db->query($query);
        }
        
        // Care va fi iesirea acestei metode?
        if ($result) {
            $response[] = array('status' => '1');
        } else {
            $response[] = array('status' => '0');
        }
        
        // Trimite raspunsul utilizatorului
        $this->response($this->json(array('registerID' => $response)), 200);
     }
     
     /*
      * Functie necesara pentru a obtine ultimile evenimente din sistem.
      */
     private function api_latest_events(){           
        $start_index = intval($_GET['start_index']);
        $per_page = intval($_GET['per_page']);
			
        $query = "SELECT SQL_CALC_FOUND_ROWS deal_id, title, DATE_FORMAT(end_date, '%D %M %Y') as end_date,     
                   after_discount_value, start_value, icon, image
                  FROM tbl_deals d, categories c 
                  WHERE d.category_id = c.id AND CURDATE() <= d.end_date 
                  ORDER BY deal_id DESC 
                  LIMIT $start_index, $per_page";
        $sql = $this->db->query($query);
        $result = array();
        $result_total = 0;
        if ($sql !== false) {
            while ($row = mysqli_fetch_array($sql, MYSQLI_ASSOC)) {
                $result[] = $row;
            }
            
            $query = self::SQL_SELECT_NUM_ROWS;
            $sql = $this->db->query($query);
            $row = mysqli_fetch_array($sql, MYSQLI_ASSOC);
            $result_total = $row['count'];
        }
            
        $this->response($this->json(array('count' => $result_total,
                                          'latestDeals' => $result)), 200);
    }
    
    /*
      * Functie necesara pentru a obtine ultimile evenimente din sistem.
      */
     private function api_latest_events1(){           
        $start_index = intval($_GET['start_index']);
        $per_page = intval($_GET['per_page']);
        
        // Numarul de milisecunde trecute de la 1 Jan 1970.
        $now = time() * 1000;
			
        $query = "SELECT SQL_CALC_FOUND_ROWS e.id AS eventId, title, startDate, endDate, icon, image
                  FROM events e, categories c 
                  WHERE e.cid = c.id AND $now <= e.endDate 
                  ORDER BY e.id DESC 
                  LIMIT $start_index, $per_page";
        $sql = $this->db->query($query);
        $result = array();
        $result_total = 0;
        if ($sql !== false) {
            while ($row = mysqli_fetch_array($sql, MYSQLI_ASSOC)) {
                $result[] = $row;
            }
            
            $query = self::SQL_SELECT_NUM_ROWS;
            $sql = $this->db->query($query);
            $row = mysqli_fetch_array($sql, MYSQLI_ASSOC);
            $result_total = $row['count'];
        }
            
        $this->response($this->json(array('count' => $result_total,
                                          'latestDeals' => $result)), 200);
    }
        
    /*
     * Metoda ce intoarce lista de categorii existente pe server. 
     */
    private function api_get_categories()
    {            
        // Cate categorii avem in sistem?
        $query = "SELECT * FROM categories ORDER BY name ASC";
        $sql = $this->db->query($query);
        $result = array();
        $count = 0;
        // Obtine linie cu linie 
        while ($row = mysqli_fetch_array($sql, MYSQLI_ASSOC)) {
            $result[] = $row;
            $count++;
        }
        // Trimite raspunsul...    
        $this->response($this->json(array('count' => $count,
                                          'categories' => $result)), 200);
    }
    
    /*
     * Metoda prin care se cauta in sistem un eveniment dupa anumite cuvinte cheie.
     */
    private function api_events_search()
    {
        $keyword = $_GET['keyword'];
        $start_index = intval($_GET['start_index']);
        $per_page = intval($_GET['per_page']);
            
        // Prin intermediul cuvantului cautat se poate specifica si dupa ce
        // criterii se poate face aceasta cautare. In mod implicit, cautarea
        // se va face dupa titlu.
        $pattern = '/^([a-zA-Z]+):(.*)$/';
        $afterColumns = array(
            'name' => 'title',
            'addr' => 'address',
            'desc' => 'description'
        );
        $searchColumn = 'title';
        if (preg_match($pattern, $keyword, $matches) === 1) {
            $after = $matches[1];
            $keyword = $matches[2];
            $column = $afterColumns[$after];
            if (isset($column)) {
                $searchColumn = $column;
            }
        }
        
        // Numarul de milisecunde trecute de la 1 Jan 1970.
        $now = time() * 1000;
        
        // Nu se permit cautari ale caror cuvinte cheie contin mai putin de 4
        // caractere. Nu dorim sa returnam sute sau chiar mii de intrari.
        $result = array();
        $result_total = 0;
        if (strlen($keyword) > 3) {
            $query = "SELECT SQL_CALC_FOUND_ROWS e.id AS eventId, title, startDate, endDate, image, icon
                      FROM categories c 
                      INNER JOIN events e ON e.cid = c.id 
                      WHERE e.$searchColumn LIKE '%$keyword%' AND $now < e.endDate
                      ORDER BY e.endDate ASC 
                      LIMIT $start_index, $per_page";
            $sql = $this->db->query($query);
        
            if ($sql !== false) {
                while ($row = mysqli_fetch_array($sql, MYSQLI_ASSOC)) {
				    $result[] = $row;
                }
            
                $query = self::SQL_SELECT_NUM_ROWS;
                $sql = $this->db->query($query);
                $row = mysqli_fetch_array($sql, MYSQLI_ASSOC);
                $result_total = $row['count'];
            }
        }
        $this->response($this->json(array('count' => $result_total,
                                          'searchEvents' => $result)), 200);
	}
    
    private function api_events_around()
    {
        $latitudeRef = floatval($_GET['latitude_ref']);
        $longitudeRef = floatval($_GET['longitude_ref']);
        
        // In mod implicit raza maxima de detectie este de 10 mile.
        // Dar poate fi suprascrisa cu usurinta.
        $radius = 10;
        if (isset($_GET['radius'])) {
            $radius = floatval($_GET['radius']);
        }
        
        // Sortare si selectia eveimentelor se va face direct din MySQL
        $query = "SELECT e.id AS eventId, title, address, latitude, longitude, icon, ((ACOS(SIN($latitudeRef*PI()/180)*SIN(e.latitude*PI()/180)+COS($latitudeRef*PI()/180)*COS(e.latitude*PI()/180)*COS(($longitudeRef-e.longitude)*PI()/180))*180/PI())*60* 1.1515) AS distance
                  FROM events e, categories c
                  WHERE e.cid = c.id
                  HAVING distance < $radius
                  ORDER BY distance ASC";
        $sql = $this->db->query($query);
        
        // In cazul in care o eroare apare, atunci se va returna o lista goala de evenimente.
        $result = array();
        if ($sql !== false) {
            while ($row = mysqli_fetch_array($sql, MYSQLI_ASSOC)) {
                $result[] = $row;
            }
        }
            
        $this->response($this->json(array('eventsAround' => $result)), 200);
   	}
    
    private function api_events_add() {
        $title = urldecode($_GET['title']);
        $address = urldecode($_GET['address']);
        $cid = intval($_GET['category_id']);
        $startDate = intval($_GET['start']);
        $endDate = intval($_GET['end']);
        $cid = intval($_GET['category_id']);
        $image = urldecode($_GET['image']);
        $latitudeRef = floatval($_GET['latitude_ref']);
        $longitudeRef = floatval($_GET['longitude_ref']);
        
        $result = array();
        $result['status'] = 1;
        
        // Verifica daca imaginea dorita exista deja pe server.
        // Inregistrarea evenimentului trebuie sa fie mereu al doilea pas - si ultimul.
        if (!file_exists("./../upload/images/$image")) {
            $result['status'] = 0;
            $result['message'] = "The image specified doesn't exists.";
        }
        
        if ($result['status'] == 1) {
            $query = "INSERT INTO events VALUES (NULL, '$cid', '$title', '$address', '$latitudeRef', '$longitudeRef', '$startDate', '$endDate', '$image', '')";
            $sql = $this->db->query($query);
        
            if (!$sql) {
                $result['status'] = 0;
                $result['message'] = "Error";
            }
        }
        
        $this->response($this->json($result), 200);
    }
             
    private function json($data){
	   if (is_array($data)) {
	       return json_encode($data);
	   }
	}
}
 	
// Instantiaza libraria principala.
$api = new API;
$api->process_api();    
?>