Cameron Bethell - A00892411 - COMP 4932 - Assignment 3
--------------------------------------------------------
The program is simple to use. When it is running, it will continuously try to match the best face from the database
to the face it finds in the webcams captured image.

--------------------------------------------------------

My code is all in src/cam.
The bulk of the algorithms are in camFacial.java for face finding, scaling, blurring

--------------------------------------------------------

My facial recognition project uses the webcam to find best matched faces in real time.

It initially uses motion detection until it finds a face from the motion that is a good enough face match.
From there, it switches to head tracking until the face match is not good enough.

A guassian blur is used to better match and eliminate the details of the background and improve facial matching.

Whenever the camera draws a box to get a best match face it uses sizes bigger and larger than that guess to also
try and find a best matching face, in case they are moving close to the camera or further away.

Using the previous students code, the facial recognition program is continuously trying to find a best match face
based on the existing face database.