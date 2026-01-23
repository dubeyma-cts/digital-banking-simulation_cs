Background
The purpose of this case study is to provide a implementation guide for developers.
This is to achieve a easy transition from ASP to ASP.Net projects.

Overview
The case study is for an Online Banking Application for a fictitious bank named Indian Net Bank (iNB). The proposed application must have some basic features for customers to perform their day-to-day banking operations over the Internet.
 
Apart from day-to-day operations, the site must also take into consideration security, ease of use, familiar look-and-feel across the application, easy navigation and performance.
 
Application will also have to be deployed and  tested on IIS. 
 
Requirements
Customer Registration
The customer should be able to register online to open an account with iNB. During registration, the customer will specify the type of account he wants to open. The registration request will be sent to the bank for an approval. The customer will be registered & the account will be created by the bank after the approval. The bank will send a letter to the customer once the registration is done. The customer should be able to logon to the site only after that. Customer can either have a Savings account or a Current account.
Home Page
The customer’s home page should be displayed after login. The home page should display the customer’s account information with balance.
Accounts
Customer can have two types of accounts; viz; Savings and Current. Customer should be able to see a Mini (last 5 transactions) & a Detailed (Date range) statement for an account.
Savings Account
Savings account, in addition to other basic information, will have a rate of interest, which customers can earn on their monthly balances.
 
All savings accounts in the bank will have the same rate of interest. If the rate changes, it will be effective for all savings accounts.
 
Customers should not be able to withdraw more than a configured amount on a day. A minimum balance must be maintained at all times. This amount must be configurable.
Current Account
Current accounts will not have a rate of interest as customers cannot earn interest on a current account.
 
A current account can have the facility of having a zero balance.
 
Customers should be able to avail of overdraft facility on their current accounts. If overdraft facility is availed, then the bank will charge a rate of interest on the overdraft amount to the customer. This rate of interest will be same for all current accounts, irrespective of the amount being withdrawn as overdraft.
 
Overdraft Charges Calculation
Overdraft charges will be calculated daily. So at the end of the day, if the balance is negative, overdraft charges will be added to the balance, resulting in the balance going down further in negative territory.
Cheque Deposits
The customer can deposit cheques issued against his name by filling online bank slip(s). After filling in the details, the customer can print & attach the slips to the cheques. The cheques along with the bank slips will be sent to the bank by post/courier.
 
The banker will see a list of all the online slips submitted by all the customers.  By default, the status of cheques will be ‘Not received’. Once the cheques are received by the bank & sent for clearance, the status will be changed to ‘Sent for Clearance’. Cheques are usually cleared in 3 working days. The status of all the cheques that are cleared is changed to ‘Cleared’ & the customer’s account gets updated. If a cheque bounces, the status is changed to ‘Bounced’. Once a cheque bounces, a fine amount is deducted from the customer’s account. A bounced cheque will have to be resubmitted for clearance.
 
The customer should be able to check the status of all the deposited cheques online.
 
Reconciliation
A report will be printed as on a particular date, which will list all the cheques that have been received, and out of these received cheques, how many have been cleared, bounced and not-cleared.
Bill Payments
Customer can make online payments to utility services like electricity, telephone, cell phone etc. from his account. Bills can be paid immediately or they can be scheduled for payment at a later date.
Money Transfer
Customer can also transfer money to any iNB user account.
Look-and-feel
All pages must have the same look-and-feel with exactly the same header, footer and navigation panel on the left.
 
Users must be able to go to any screen with minimum mouse-clicks.
Security
After three consecutive invalid logon attempts, a user’s login account should be locked. This login account can only be re-activated manually.