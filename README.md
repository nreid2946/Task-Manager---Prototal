# Project Title

![Build Status](https://img.shields.io/badge/build-passing-brightgreen) ![Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen) ![Version](https://img.shields.io/badge/version-1.0.0-blue)

A CRUD app per the requirements outlined in Prototal's stage II interview document.

## Installation

First clone this repository to a directory of your choosing. Then choose your preferred installation method.

## Docker

Ensure you are in the project's root directory and:

First run:
```bash
    docker-compose build
```
Then:
```bash
    docker-compose up
```

This should make the project run, and you can begin pinging the endpoints at the bottom of this article.

If you run into issues, ensure that the Docker desktop client is running.
    
If that again does not work, try running manually (below).

## Installation | Manual
```bash
# Clone this repository
git clone https://github.com/yourusername/yourprojectname.git

# Navigate to the project directory
cd x

# Install dependencies
mvn clean install

# Run the program.
Use a popular code editor like Intellij IDEA or Eclipse, and run the TaskManagerApplication.java file.
```

## CRUD
You can run CRUD operations by pinging the endpoints at localhost:8080.

For example:

```bash
http://localhost:8080/tasks/getAll
```

(This will initially return empty since the database isn't initially populated.)

## Q&A
Potential issues can arise from having the application.properties set to the wrong location.

Ensure it points to spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI}.

## Endpoints
We have several endpoints which can be pinged in the formats below:

ROOT: @RequestMapping("/tasks")

This is the root of the URL, meaning that our API is based around it. Requests will be in the format:

http://localhost:8080/tasks/(request)

**@PostMapping("createTask")**

Path: http://localhost:8080/tasks/createTask

Body example:

```json
{
    "title": "A totally amazing task.",
    "description": "Read a book.",
    "subTasks": [
        {
            "title": "Read a chapter.",
            "description": "Read that super boring chapter."
        },
        {
            "title": "Read page of book.",
            "description": "This is so fun.",
            "subTasks": [
                {
                    "title": "Read line 12",
                    "description": "This is getting specific.",
                    "subTasks": [
                        {
                            "title": "Read the first letter of line 12.",
                            "description": "..."
                        }
                    ]
                }
            ]
        }
    ]
}
```

**@GetMapping("getById/{id}")**

Path example: http://localhost:8080/tasks/getById/id

**@PutMapping("/update/{id}")**

Path example: http://localhost:8080/tasks/update/id

**@DeleteMapping("deleteTask/{id}")**

http://localhost:8080/tasks/deleteTask/id

Also:

**@GetMapping("getAll")**

Path example: http://localhost:8080/tasks/getAll

**This is a utility method for easily retrieving entries to the db. The reason this is included is because it can be 
otherwise quite difficult to get the _ids since it is sent to automatic _id
generation.**