**4. Design Diagrams
4.1 High-Level System Architecture Diagram
Description:**
The Online Banking Application for Indian Net Bank (iNB) follows a centralized web-based architecture. Customers access the system through a standard web browser. Requests are processed by an ASP.NET application hosted on IIS, which communicates with a backend SQL Server database.
 
**Architecture Flow:**
•	Client requests are initiated from the customer’s web browser.
•	IIS hosts the ASP.NET application and manages request handling, session management, and security.
•	Business logic is executed in a separate Business Logic Layer (BLL).
•	All persistent data is stored and retrieved from the SQL Server database through a Data Access Layer (DAL).
Architectural Benefit:
This design enforces separation of concerns and simplifies migration from Classic ASP to ASP.NET by clearly isolating presentation, business logic, and data access.

**4.2 Layered Architecture Diagram**
The application is designed using a multi-layered architecture to improve maintainability, scalability, and reusability.

**Layers:**
**Presentation Layer**
•	ASP.NET web pages
•	Master Pages for consistent layout
•	User Controls for reusable UI components
**Business Logic Layer**
•	Customer registration and approval
•	Account management (Savings and Current)
•	Transaction processing
•	Interest and overdraft calculations
•	Security and validation logic
**Data Access Layer**
•	ADO.NET components
•	Stored procedures for all database interactions
**Database Layer**
•	SQL Server database
•	Tables for customers, accounts, transactions, cheques, and payments
**Architectural Benefit:**
Unlike Classic ASP applications where logic and database access were often embedded in UI pages, ASP.NET enforces a structured approach, making the system easier to maintain and extend.

**4.3 Component / Module Diagram
Description:**
The application is divided into functional modules, each responsible for a specific banking operation.
 
**Major Modules:**
•	User Registration & Authentication
•	Account Management
•	Transaction & Statement Management
•	Cheque Deposit Processing
•	Bill Payment Processing
•	Money Transfer
•	Reporting & Reconciliation
•	Security & Access Control
**Architectural Benefit:**
Each module is loosely coupled and communicates through well-defined interfaces, enabling independent development and future enhancements.

**4.4 Deployment Diagram
Description:**
The application is deployed in a two-tier physical architecture with logical separation of application and database servers.

**Deployment Components:**
•	Client Machine (Web Browser)
•	Web Server (IIS + ASP.NET Application)
•	Database Server (SQL Server)
**Architectural Benefit:**
This deployment model improves security and performance by isolating database operations from the web tier and allows independent scaling of application and database servers.
**5. Conclusion (for your section)**
The proposed design provide a robust, scalable, and secure foundation for the Online Banking Application. By adopting ASP.NET architectural best practices, the solution enables a smooth transition from Classic ASP while improving maintainability, performance, and security.


