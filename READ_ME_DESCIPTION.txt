used a library for magic number to check file type and handle accordingly
only plays audio and video files supported by javafx with simple controls [play, pause, stop, shows length of media in seconds, has a slider so the user can skip around the media] (audio will still play if the window is closed and the stop button was not pressed first, no idea how to fix)
can open jpeg and png images 
can open pdf files and show multiple pages with next and previous buttons for control
opens word powerpoint and excel documents by checking if the corresponding application is installed. for word docs, if word is not available it will open in a textarea. the others will show an error. i tried to find apis that work with maven to open them online but i gave up on that.
opens text files
can extract zip files to a specific location of the users choosing

classes created
MediaPlayerController
PDFViewer
MicrosoftDocuments
ImageViewer
CompressedFileHandler
CheckFileType

modified class FileBowserWindow to work with the new classes

added maven for dependencies needed and added a .fxml file to handle the javafx required for the class MediaPlayerController

i did this using the first version of the code so the search implementation is not there 