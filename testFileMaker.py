import string
import random

c = [random.choice(string.ascii_letters) for i in range((2**20) * 10)] # 10 MegaByte Files

data = "".join(c)

num_files = 10 # Number of files to generate
num_edits = 10 # Number of random edits to each file
for i in range(num_files): # Make 10 files
	cpy = data
	for j in range(num_edits): # Make 10 random edits
		edit = random.randrange(3) # Randomly select insert, delete or replace
		location = random.randrange(len(cpy)) # Randomly select a location in the file
		if edit == 0: # Insert
			cpy = cpy[:location] + random.choice(string.ascii_letters) + cpy[location:]
		elif edit == 1: # Delete
			cpy = cpy[:location] + cpy[location+1:]
		elif edit == 2: # Replace
			cpy = cpy[:location] + random.choice(string.ascii_letters) + cpy[location+1:]

	f = open("test" + str(i) + ".txt", "w")
	f.write(cpy) # Write data to file
	f.close()
