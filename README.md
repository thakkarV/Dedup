# Group Summary
ENG EC 504 - Advanced Data Structures and Algorithms - Fall 2017
Vijay Thakkar, Class of 2019
Reetpragya Chowdhary, Class of 2018
Rahul Jain, Class of 2018
JIRA link: https://agile.bu.edu/jira/projects/EC504PG4/summary

# Problem at Hand:
Traditional compression algorithms like LZ are only intra-file compression, which means that if there are many files with similar content, they still require large storage space on disk. Deduplication is a method of inter-file compression which takes into account similarities between several files and allows for storage of these files in smaller chunks while only storing multi-referenced chunks once.

# Documentation
## High Level Summary:
Our approach uses a Rabin Karp rolling hash to establish fingerprints on the input files given to our program. Using these fingerprints we create content-based FileChunks from the file and hash these FileChunks. Once we hash the chunks we store the hash and the chunk together in a HashMap with the hash being the key and the FileChunk being the value. The files are then represented as an ArrayList of hashes which can be used to query the FileChunks out of the HashMap and reassemble the file FileChunk by FileChunk. Due to our content based chunking strategy, single character edits will only affect the hash of the chunk that they reside in but the other chunks will remain the same thereby allowing us to deduplicate these files with a high compression ratio. We implement reference counting of these chunks, so if a duplicate chunk is found, then we simply increment its metadata reference counter. This also makes delete rather trivial to implementation.

## List of Features
### Ability to store and retrieve ASCII files in any order at any time in a portable locker with a CLI interface
We met the baseline requirements with our initial algorithm and further extended it with the following other features.
### GUI
The GUI allows for the creation of a locker, opening of a locker and adding, deleting and retrieving files from an open locker.
### Ability to store directories of files as one entity
Although this was not in our initial feature list we were able to achieve storing and retrieval of a directory by using our addFile algorithm which had already been created and worked well.
### Implementation of file deletion from the locker
Although this was not in our initial feature list we realized that our method of storing the chunks could be easily modified to add a simple reference counter which would allow for very efficient and simple deletion.

We did not implement efficient storage of images to the locker as our fingerprinting algorithm works well with ascii text files and was not as portable to images. We also didn’t implement networked access because we didn’t have a remote storage medium

## References
http://ieeexplore.ieee.org/document/5581583/

## The locker Class
### Adding Files and Directories
Files are added by first getting the RK-fingerprint chunk boundaries using the Rabin Fingerprint class as described in the next section. Once the chunk boundaries are obtained, the original file is chopped up into chunks, and a MD5 hash is calculated. This hash-chunk pair is now inserted into the main Locker chunkMap if it does not exist. If it does exist, then we increment the reference counter for that chunk. Together with this, to keep track of which chunks belong to which file, a data structure called LockerFile keeps an ordered array list of all hashes of the chunks that belong to the file. This is serialized separately from the hashmap to disk in a “.files” directory inside the locker. If a directory is being added, it simply calls addFile on all the files to be added in different threads, using Reader-Writer locks to prevent race conditions while inserting into the chunkMap or the LockerFile list while still allowing high read throughput. When all files have been added, the added files or directories are stored in the “.flies” subdirectory of the locker and the chunkMap hashmap is stored in the “.locker” subdirectory.

### Retrieving Files and Directories
First the hashmap and the file to be retrieved are loaded back into the locker by deserializing them. Note that the deferred deserialization implemented for the locker means that only the files to be retrieved need to be loaded back in, and not the rest, saving I/O bottleneck and process memory. Then we simply build the byte array of the original file back by getting the chunks of the file from the hashmap by doing a Key Value pair lookup, where the keys are the ordered array of Chunk Hashes in the LockerFile data structure of the saved file. Once this byte array is built, we write it as a file to the output path. For restoring a saved directory, we do this again in a multithreaded way, each thread working on one file with Reader Writer locks to manage concurrency. This offers large speedups, and in our testing, retrieving 50 files, each 10 MB (differing by 10 edits, saved as a directory in locker), only takes about one second. 

### Deleting Files and Directories
Each FileChunk data structure also keeps an integer counter as metadata. This is a reference counter of how many files in the locker contain this block. When we delete a given file, first the serialized file is load from “.files” directory of the locker, and all its Chunk hashes obtained. Then we decrement this reference counter of all the hashes in the map belonging to this file and if the counter reaches zero, remove that KV pair (Hash-Chunk pair) from the hashmap. Then we delete the serialized file from disk itself, and rewrite the hashmap to disk. If we are deleting a directory, we simply multithread the deletion of all files in the directory to be deleted, making sure to use locks to prevent race conditions in critical sections (removing or decrementing references etc.).

## Rabin-Karp Fingerprint

In order to determine the boundaries for our chunks, we utilize Rabin-Karp Fingerprinting. We create a Rabin fingerprint of a window at the beginning of a file and proceed to slide this window across the file to create successive fingerprints. If the fingerprint of a window meets specific criteria (based on chunk size requirements) then we establish a chunk boundary.
Utilizing a polynomial to generate the fingerprint such that we need constant time to move the window one step means that this fingerprinting works in O(n) time with n being the length of the file.
We can control the number of unique fingerprints by providing a rollover modulus value, and make a polynomial such that we get a reasonably uniform distribution of fingerprints. Now by knowing the nature of this distribution we can ascertain the boundary conditions for our chunks.

##GUI
The GUI for our deduplicator is a simple Java Swing application. It consists of a JList component, a JMenuBar and three JButtons. We separated our code through the MVC pattern and so we have a GUIListener class in the controller package which listens for button actions and accordingly triggers the corresponding locker functions. For example pressing the add button will open a JFileChooser from which the user can pick a file to deduplicate. Once they accept, the GUIListener then calls locker.add(file.getPath()) and locker.save() to trigger the changes to occur in the locker. All changes made by delete and add are reflected in the JList.


# Work Breakdown

## Reet Chowdhary
Reet designed and implemented the frontend GUI code which allows the user to add, delete and retrieve their files and directories in the deduplicator through a simple interface. Reet also worked on the Rabin Fingerprinting algorithm along with Rahul to establish the chunk boundaries specifying where to hash.

## Vijay Thakkar
Vijay designed and implemented the locker class, including the methods for adding files and directories using the chunk boundaries from the fingerprinter, multithreading of directory operations, proving the thread safety of the code base, the ability to delete inserted files and directories and the method of reference counting to supporting the delete feature, the retrieval methods for getting back inserted files and directories from locker as well as the custom serialization and deserialization to save and load the locker from disk and the deferred serialization/deserialization system to save on disk I/O.. In addition to this, he also designed and implemented the supporting classes for the locker: FileChunk and LockerFile.

## Rahul Jain
Rahul implemented the Rabin fingerprinting algorithm which is used to define boundaries to break the file into smaller chunks for storage. The ability to control chunk size is provided within the fingerprinting algorithm.
