# Reservation Code Challenge


ParkHere code assignment
LICENSE

Copyright (C) ParkHere GmbH - All Rights Reserved
Unauthorized copying of any content of this project via any medium is strictly prohibited, Proprietary and confidential.

Introduction
=============================
Hello and welcome!

As part of our interview process, we would like you to complete a programming assignment. It presents a hypothetical situation which resembles how you could be solving problems at ParkHere. 

Please read the whole description thoroughly, then create a program to solve the problem at hand.
Please use Java 11+ or Kotlin for your implementation. The application must run and your solution should provide sufficient evidence that it is complete.

### Note 1: 
Feel free to use any application frameworks such as Spring Boot or DropWizard for your implementation, however developing the solution with AWS lambda functions will be considered as an extra advantage for you.

### Note 2: 
You are also free to use any database, middleware (messaging platform, etc.) of your choice which suits best for your application according to the given requirements.


## Parking Reservation System

ParkHere has decided to develop a product that allows users to reserve parking spots of a car park through our mobile app before they get there. Therefore, when they arrive they would know exactly where to park. We want a solution built based on loosely coupled microservices which follow the general principles of high-demand distributed systems such as scalability, availability and reliability.

We would like to run our business in many countries - therefore, the system should to be scalable according to our customer demands. We’re also concerned about the performance and therefore aim for a 500ms response time from our reservation system.

## Part 1: Parking Reservation (coding)

You have access to the `configuration-microservice` which gives you a list of parking spots in a given parking lot. 

The service endpoint is `GET /api/parking-lots/{parking-lot-id}` and will return a list of available parking spots for a given location as below:

```
[
    {
        id: 1,
        spotName: "spot1",
        priority: 1
    },
    ...
]
```

You can find a json file attached to use as example.

Now, we would like you to implement the endpoint below to create reservations in the given parking lot based on the list above.


`POST 	/api/parking-lots/{parking-lot-id}/reservations`

```
ReservationRequest: {
    "user-id": "john@park-here.eu",
    "startTimestamp": 1737586800000,
    "endTimestamp": 1737627502000
}

ReservationResponse: {
    "id": 123,
    "spotId": 1,
    "startTimestamp": 1737586800000,
    "endTimestamp": 1737627502000,
}
```

The reservation service must meet the following requirements:

- Reservations should be stored permanently.
- Users should be able to reserve for any time frame with given `startTimestamp` less than `endTimestamp`. 
- When making a reservation, the user should be given the spot with the highest priority, or informed if no spots are available
- Multiple reservations for one user are possible unless the time frames overlap
- No two users can reserve the same spot for overlapping timespans

### Valid assumptions:

The below microservices are provided to you as an option, so you may use any of them within your solution if you deem them helpful. There is no need implement them.

However, you do need to specify the operations’ signature for that particular microservice if you decided to use it in your solution so we would know what would you expect from that microservice in order to solve the task at hand.


- The information about parking lot such as its capacity, location and etc. are all defined within `configuration-microservice` 
- No security mechanism (`authentication` and `authorization`) would be needed for this code assignment
- Entities can be created with minimum required properties

## Part 2: System Design

We would like you to use a drawing tool such as https://www.draw.io to illustrate your design for the major components of such a system in terms of microservices, their interaction styles, preferred databases and any other components you might need for your solution. 

## Part 3: Deployment (Optional - Bonus)

As a bonus, we would like you to describe a deployment plan for you solution, leveraging a cloud provider of your choice.

AWS is preferred, but other providers are acceptable.

At ParkHere, we use Terraform. 


## Tips ##

* We value writing clean code. Your code will be reviewed by our developers, so make sure it is easy to follow and well-structured.

* Don't feel the need to over-engineer your solution. We don't expect you to build an entire system that can scale to billions of events. Your solution should be tailored to the problem statement.

## How to submit the solution

As of now, you have given access to the private repository for this challenge.
All you have to do is to fork this repository to a new private repository in the git provider of your choice.

Once you’re done with the implementation, kindly invite our colleagues to review your solution.

* andac.kurun@park-here.eu
* jakob.mezger@park-here.eu
* mirzet.brkic@park-here.eu
* massimiliano.gerardi@park-here.eu

Please make sure you include a high quality image of your drawing within the solution or provide a link if you’re using an online tool.


Good luck!  
ParkHere Dev Team

