You can call our APIs in batch mode using CSV or TSV files as input. While you can call the APIs repeatedly yourself, using our batch tool eliminates web latency by uploading the file, running the job, and making the results available for download. Using the tool can be 10x-50x faster than calling the API over and over for large files and data volumes.



The tool also generates the corresponding "Full Dataset API" that the batch tool uses itself with filenames and selected API as input parameters. The results are printed to the console, enabling this batch process to be implemented and automated within a pipeline, within ELT/ETL, or a script if desired.



https://batch.interzoid.com

