# Title and Purpose of Application
## Vacation Planner
Vacation Planner is an android mobile application built using Android Studio IDE (Ladybug). This application serves as an introduction to programming for android devices, utilizing a software development kit (SDK). Vacation Planner was built using a graphical user interface (GUI) and includes CRUD (create, read, update and delete) functionality through the SQLite database.

Vacation Planner allows users to keep track of vacations. Users can enter information about their vacation- the title, hotel name, start date and end date. Users can also add associated excursions, containing a title and date, to each vacation. Vacation planner allows users to change or delete vacation and excursion details. 

The restrictions on changes/deletions to vacations and excursions include: 
* Vacations can be deleted once there are no associated excursions. 
* Excursions can be changed as long as the date falls within the selected vacation start and end dates.

The application also allows users to set up alerts for both vacations and excursions. This notification goes off even if the application is closed. For vacations, the user can choose to be alerted on the vacation start and end dates. They will see a notification stating whether the vacation is starting or ending. For excursions, the user can choose to be alerted on the excursion date and will see a notification stating that the excursion is starting.

Finally, the app also allows users to share the vacation details. All of the vacation details, including any associated excursion details, can be shared through email, text message or other messaging apps. The user also has the option to copy the vacation details to the device's clipboard to be manually shared.

# Application Directions
## Set-up
1. Clone the project to the Android Studio Ladybug IDE:
* File > New > Project from Version Control...
* For the URL, use: https://gitlab.com/wgu-gitlab-environment/student-repos/dmahar3/d308-mobile-application-development-android.git
* Click Clone

2. Open the Device Manager and click the + icon to add a new virtual device. This sets up the emulator to run the Vacation Planner application. In the Device Manager panel, you can also choose to pair a physical android device using wifi.

3. Run the app to view the Vacation Planner on the emulator.

4. To ensure alerts function correctly, navigate to the device Settings > Notifications > App Notifications > All apps. Scroll down to the _Vacation Planner_ app and toggle the switch on to enable receiving notifications from the app.

## User guide
* From the Vacation Planner home page, click the "Get Started" button to enter the application. You will be taken to the _Vacation List_ screen. Click the overflow button (3 dots) in the top right corner to bring up the menu items. Click "Add Sample Code" to add two vacations to the list. The page will refresh once the code is added.

* **To add a new vacation to the list:** Click the blue plus sign button at the bottom of the _Vacation List_ screen. You will be taken to the _Vacation Details & Excursions_ screen. Here you can enter details for the vacation. There are 2 text fields to type in the vacation title and a hotel name. There are 2 buttons to select a start date and end date from a pop-up calendar. To save a vacation, the end date must be after the start date- a warning message will be shown otherwise. You must also save the vacation first before adding excursions- a message will be shown if you try to add one before saving. When the information is entered, click the overflow button in the top right corner to bring up the menu items. Click "Save Vacation." A message will be shown stating your vacation was added. The app will then refresh to the previous screen, _Vacation List_, showing your newly added vacation in the list.

* **To add an excursion to a vacation:** Click on a vacation title on the _Vacation List_ screen. On the _Vacation Details & Excursions_ screen, click the blue plus sign button at the bottom of the screen to add an excursion to the selected vacation. You will be taken to the _Excursion Details_ screen. There is a text field to type in an excursion title and a button to select a date for the excursion. The excursion date must be within the associated vacation's start and end dates or a warning message will be shown when you go to save it. To save the excursion, click the overflow button in the top right corner to bring up the menu items. Click 'Save Excursion.' A message will be displayed confirming the excursion was added. The app will refresh back to the previous screen, _Vacation Details & Excursions_, to show the added excursion below the vacation details.

* **To update a vacation:** From the _Vacation List_ screen, click on the vacation title. This will take you to the _Vacation Details & Excursions_ screen. Change any of the details for the existing vacation. The date restriction will apply if you are changing the vacation dates- the end date must be after the start date. Once you are satisfied with the changes, access the menu items from the overflow button again and click "Save Vacation." A message will be displayed confirming the vacation was updated.

* **To update an excursion:** From the _Vacation Details & Excursions_ screen, click on the excursion title. The date restriction applies if you are changing the date- the selected date must be during the vacation duration. Once changes are made, click the overflow button in the top right corner to view the menu and click "Save Excursion." A message will be displayed confirming the excursion was updated.

* **To set up a vacation alert:** From the _Vacation List_ page, click on the vacation title to set up notifications. On the next page, _Vacation Details & Excursions_, access the menu items from the overflow button in the top right corner. Click "Set Vacation Alert." A message will be shown confirming that the alerts have been set. The app will then send notifications on the vacation start and end dates stating the vacation title and whether it is starting or ending.

* **To set up an excursion alert:** From the _Vacation List_ page, click on the vacation title to view the associated excursions. On the _Vacation Details & Excursions_ page, click the excursion title to set up an alert. On the _Excursion Details_ screen, click the overflow button in the top right corner to bring up the menu items. Click "Set Excursion Alert." A message will be shown confirming that the alert was set. The app will then send a notification on the date of the excursion stating that it is starting.

* **To share vacation details:** From the _Vacation List_ screen, click the vacation title. From the _Vacation Details & Excursions_ screen, click the overflow button in the top right corner to bring up the menu items. Click "Share Vacation Details." You will see a preview of the message to be shared, which includes all the vacation details and any associated excursion details. You can click the copy icon to copy the message to the device's clipboard to share manually; or you can select the Messaging app to send the details as a text message; or you can choose an Gmail or another email app to send the message as an email. The contents of the email or text message will be populated with the full vacation details.

* **To delete an excursion:** From the _Vacation List_ page, click on the vacation title with the associated excursion. On the _Vacation Details & Excursions_ screen, click the title of the excursion. From the _Excursion Details_ screen, click on the overflow icon in the top right corner to access the menu items. Click "Delete Excursion." A message will be displayed confirming that the excursion was deleted. The app will refresh back to the previous page, _Vacation Details & Excursions_. The deleted excursion will no longer be in the Associated Excursions list.

* **To delete a vacation:** From the _Vacation List_ screen, click on the title of the vacation you would like to delete. On the _Vacation Details & Excursions_ screen, access the menu items by clicking on the overflow button in the top right corner. Click "Delete Vacation." In order to delete a vacation, there cannot be any associated excursions- a message will be shown with this warning if a vacation has any added excursions. Once successful, a message will be shown confirming the vacation was deleted. The app will refresh to the previous screen, _Vacation List_, where you will no longer see the deleted vacation in the list.

# Android Version
This app is built on Android 15.

# Link to Git Repository
[https://gitlab.com/wgu-gitlab-environment/student-repos/dmahar3/d308-mobile-application-development-android.git](https://gitlab.com/wgu-gitlab-environment/student-repos/dmahar3/d308-mobile-application-development-android.git)

