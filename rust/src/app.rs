use std::io::{self, BufRead};
use std::path::PathBuf;

use crate::command::{self, Command};
use crate::storage::Storage;
use crate::task::{Task, TaskKind};

const BANNER: &str = r"      _                                                   _       _
     | |__   ___ _ __ _ __ ___   ___  ___       _ __ ___ (_)_ __ (_)
     | '_ \ / _ \ '__| '_ ` _ \ / _ \/ __|_____| '_ ` _ \| | '_ \| |
     | | | |  __/ |  | | | | | |  __/\__ \_____| | | | | | | | | | |
     |_| |_|\___|_|  |_| |_| |_|\___||___/     |_| |_| |_|_|_| |_|_|";
const INDENT: &str = "     ";
const DIVIDER: &str = "    ____________________________________________________________";
const MAX_TASKS: usize = 100;

/// Owns the task list and executes commands independently of the user interface.
pub struct App {
    storage: Storage,
    tasks: Vec<Task>,
}

/// A response produced after handling one line of user input.
pub struct Reply {
    pub text: String,
    pub should_exit: bool,
}

impl App {
    pub fn new(data_file: PathBuf) -> Self {
        let storage = Storage::new(data_file);
        let tasks = storage.load().unwrap_or_default();
        Self { storage, tasks }
    }

    /// Runs the terminal front end using the same response API as the GUI.
    pub fn run(mut self) {
        print_greeting();
        for line in io::stdin().lock().lines() {
            let Ok(line) = line else { break };
            let reply = self.respond(&line);
            print_message(&reply.text);
            if reply.should_exit {
                break;
            }
        }
    }

    /// Handles one command and returns text that any front end can display.
    pub fn respond(&mut self, input: &str) -> Reply {
        match command::parse(input) {
            Ok(Command::Bye) => Reply {
                text: "Bye. Hope to see you again soon!".to_owned(),
                should_exit: true,
            },
            Ok(command) => Reply {
                text: self.execute(command),
                should_exit: false,
            },
            Err(error) => Reply {
                text: format!("OOPS!!! {}", error.message()),
                should_exit: false,
            },
        }
    }

    fn execute(&mut self, command: Command) -> String {
        match command {
            Command::Bye => unreachable!("bye is handled by respond"),
            Command::List => self.task_list(),
            Command::Mark { number, done } => self.mark_task(number, done),
            Command::Delete { number } => self.delete_task(number),
            Command::Find { keyword } => self.find_tasks(&keyword),
            Command::Todo { description } => self.add_task(Task::new(description, TaskKind::Todo)),
            Command::Deadline { description, by } => {
                self.add_task(Task::new(description, TaskKind::Deadline { by }))
            }
            Command::Event {
                description,
                from,
                to,
            } => self.add_task(Task::new(description, TaskKind::Event { from, to })),
        }
    }

    fn task_list(&self) -> String {
        let tasks = format_tasks(self.tasks.iter().enumerate());
        if tasks.is_empty() {
            "Here are the tasks in your list:".to_owned()
        } else {
            format!("Here are the tasks in your list:\n{tasks}")
        }
    }

    fn find_tasks(&self, keyword: &str) -> String {
        let keyword = keyword.to_lowercase();
        let tasks = format_tasks(
            self.tasks
                .iter()
                .enumerate()
                .filter(|(_, task)| task.description.to_lowercase().contains(&keyword)),
        );
        if tasks.is_empty() {
            "Here are the matching tasks:".to_owned()
        } else {
            format!("Here are the matching tasks:\n{tasks}")
        }
    }

    fn mark_task(&mut self, number: usize, done: bool) -> String {
        let Some(task) = number
            .checked_sub(1)
            .and_then(|index| self.tasks.get_mut(index))
        else {
            return "OOPS!!! That task number is not in your list.".to_owned();
        };
        task.is_done = done;
        let description = task.description.clone();
        let message = if done {
            format!("Nice! I've marked this task as done:\n  [X] {description}")
        } else {
            format!("OK, I've marked this task as not done yet:\n  [ ] {description}")
        };
        self.with_save_status(message)
    }

    fn delete_task(&mut self, number: usize) -> String {
        let Some(index) = number
            .checked_sub(1)
            .filter(|index| *index < self.tasks.len())
        else {
            return "OOPS!!! That task number is not in your list.".to_owned();
        };
        let task = self.tasks.remove(index);
        let message = format!(
            "Noted. I've removed this task:\n  [{}][{}] {}\nNow you have {} tasks in the list.",
            task.type_icon(),
            task.status_icon(),
            task.display_text(),
            self.tasks.len()
        );
        self.with_save_status(message)
    }

    fn add_task(&mut self, task: Task) -> String {
        if self.tasks.len() >= MAX_TASKS {
            return "OOPS!!! Your task list is full.".to_owned();
        }
        let task_text = format!("[{}][ ] {}", task.type_icon(), task.display_text());
        self.tasks.push(task);
        let message = format!(
            "Got it. I've added this task:\n  {task_text}\n\nNow you have {} tasks in the list.",
            self.tasks.len()
        );
        self.with_save_status(message)
    }

    fn with_save_status(&self, message: String) -> String {
        if self.storage.save(&self.tasks).is_ok() {
            message
        } else {
            format!("{message}\nOOPS!!! I couldn't save the tasks.")
        }
    }
}

fn format_tasks<'a>(tasks: impl Iterator<Item = (usize, &'a Task)>) -> String {
    tasks
        .map(|(index, task)| {
            format!(
                "{}.[{}][{}] {}",
                index + 1,
                task.type_icon(),
                task.status_icon(),
                task.display_text()
            )
        })
        .collect::<Vec<_>>()
        .join("\n")
}

fn print_greeting() {
    println!("{DIVIDER}");
    println!("{BANNER}\n");
    println!("{INDENT}Hello! I'm hermes-mini");
    println!("{INDENT}What can I do for you?");
    println!("{DIVIDER}");
}

fn print_message(message: &str) {
    println!("{DIVIDER}");
    for line in message.lines() {
        println!("{INDENT}{line}");
    }
    println!("{DIVIDER}");
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn respond_adds_and_lists_a_task() {
        let path =
            std::env::temp_dir().join(format!("hermes-mini-app-test-{}.txt", std::process::id()));
        let _ = std::fs::remove_file(&path);
        let mut app = App::new(path.clone());

        assert!(app.respond("todo review the GUI").text.contains("added"));
        assert!(app.respond("list").text.contains("review the GUI"));

        let _ = std::fs::remove_file(path);
    }
}
