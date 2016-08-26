LOAD DATA LOCAL INFILE 
"D:\iso_country_codes.csv" 
INTO TABLE country_iso_codes
FIELDS TERMINATED BY ',' 
 LINES TERMINATED BY '\n' 
(ISO_Code, Name); 
