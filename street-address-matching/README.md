API Product Page



----------------







Street Address Matching API Product Page: https://www.interzoid.com/apis/street-address-matching







The API generates similarity keys for a given street address. A similarity key represents hundreds of possible permutations of the street address (variations, misspellings, abbreviations, typos, etc.). Then, matching by similarity key rather than the actual data itself, provides for dramatically higher match and search rates when comparing data.







<br>



There are three code examples for each language platform:



<br>



<b>example.xx</b> (extension varies based on programming language)



------------------------------------------------------------



<i>Generates a single similarity key used for matching or searching purposes:</i>



<br>



```

455 Main Street -> jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM

```



<br>

<br>



<b>append-simkeys-to-file.xx</b>



--------------------------



<i>Generates a single similarity key for each entry of a text file (see sample input file):</i>



<br>



```

1000 Fourth Street E,D-s\_xMgAYDMeS-QKmx8TUYERxwIQ2MvvYuHag-c6KP8

7000 Cleaveland Rd,3AA8t1cfCYFWTksqKc-dYovygw2rFZdX8Ca3gUpNLr4

1000 4th St. East,D-s\_xMgAYDMeS-QKmx8TUYERxwIQ2MvvYuHag-c6KP8

455 E Main St,jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM

455 East Main,jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM

455 Main Street,jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM

7000 Cleveland Road,3AA8t1cfCYFWTksqKc-dYovygw2rFZdX8Ca3gUpNLr4

9000 e calif rd,2cG04SeRG6l6ZVMXG9KKpmko94upcRCtFWCuPUY7eoU

9000 east california road,2cG04SeRG6l6ZVMXG9KKpmko94upcRCtFWCuPUY7eoU

1777 st louis rd,mR2UVAsZr0uvhGNm\_5IcLnw8d7LZeQvvDVdJgBT6FiQ

500 Browne lane suite #100,PeVZhCEmoRElwGqgIIP\_49G30ywPU5oLYEgbVxX2QwA

1777 saint louis raod,mR2UVAsZr0uvhGNm\_5IcLnw8d7LZeQvvDVdJgBT6FiQ

500 Browne ln suite 100,PeVZhCEmoRElwGqgIIP\_49G30ywPU5oLYEgbVxX2QwA

500 brown lane ste 100,PeVZhCEmoRElwGqgIIP\_49G30ywPU5oLYEgbVxX2QwA

```



<br>

<br>



<b>generate-match-report.xx</b>



-------------------------



<i>Generates a single similarity key for each entry of a text file, sorts results by simkey, and then writes clusters of matching records based on simkey to an output file:</i>



<br>



```

9000 e calif rd,2cG04SeRG6l6ZVMXG9KKpmko94upcRCtFWCuPUY7eoU

9000 east california road,2cG04SeRG6l6ZVMXG9KKpmko94upcRCtFWCuPUY7eoU



<br>



7000 Cleaveland Rd,3AA8t1cfCYFWTksqKc-dYovygw2rFZdX8Ca3gUpNLr4

7000 Cleveland Road,3AA8t1cfCYFWTksqKc-dYovygw2rFZdX8Ca3gUpNLr4



<br>



1000 4th St. East,D-s\_xMgAYDMeS-QKmx8TUYERxwIQ2MvvYuHag-c6KP8

1000 Fourth Street E,D-s\_xMgAYDMeS-QKmx8TUYERxwIQ2MvvYuHag-c6KP8



<br>



500 Browne lane suite #100,PeVZhCEmoRElwGqgIIP\_49G30ywPU5oLYEgbVxX2QwA

500 brown lane ste 100,PeVZhCEmoRElwGqgIIP\_49G30ywPU5oLYEgbVxX2QwA

500 Browne ln suite 100,PeVZhCEmoRElwGqgIIP\_49G30ywPU5oLYEgbVxX2QwA



<br>



455 E Main St,jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM

455 Main Street,jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM

455 East Main,jpI9RHfHiqb3YI9a\_l24xlUegbQblCgWloO8zgHj2KM



<br>



1777 st louis rd,mR2UVAsZr0uvhGNm\_5IcLnw8d7LZeQvvDVdJgBT6FiQ

1777 saint louis raod,mR2UVAsZr0uvhGNm\_5IcLnw8d7LZeQvvDVdJgBT6FiQ

```





