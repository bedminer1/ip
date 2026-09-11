mod app;
mod command;
mod storage;
mod task;

use std::path::PathBuf;

use clap::Parser;

use crate::app::App;

use eframe::egui;

/// A small command-line task manager.
#[derive(Debug, Parser)]
#[command(version, about)]
struct Cli {
    /// File used to persist tasks between runs.
    #[arg(long, default_value = "data/hermes.txt")]
    data_file: PathBuf,

    /// Run the original command-line interface instead of the GUI.
    #[arg(long)]
    cli: bool,
}

fn main() -> eframe::Result {
    let cli = Cli::parse();

    if cli.cli {
        App::new(cli.data_file).run();
        return Ok(());
    }

    let options = eframe::NativeOptions::default();
    let data_file = cli.data_file;
    eframe::run_native(
        "Hermes Mini",
        options,
        Box::new(move |_cc| Ok(Box::new(GuiApp::new(data_file)))),
    )
}

struct GuiApp {
    chatbot: App,
    input: String,
    messages: Vec<Message>,
}

impl GuiApp {
    fn new(data_file: PathBuf) -> Self {
        Self {
            chatbot: App::new(data_file),
            input: String::new(),
            messages: vec![Message {
                sender: Sender::Hermes,
                text: "Hello! I'm Hermes Mini. What can I do for you?".to_owned(),
            }],
        }
    }
    fn show_messages(&self, ui: &mut egui::Ui) {
        egui::ScrollArea::vertical()
            .stick_to_bottom(true)
            .show(ui, |ui| {
                for message in &self.messages {
                    match message.sender {
                        Sender::User => {
                            ui.label(format!("You: {}", message.text));
                        }
                        Sender::Hermes => {
                            ui.label(format!("Hermes: {}", message.text));
                        }
                    }
                }
            });
    }

    // Draws input field and button
    fn show_input(&mut self, ui: &mut egui::Ui) -> bool {
        let mut should_send = false;

        ui.horizontal(|ui| {
            let input_response = ui.add(
                egui::TextEdit::singleline(&mut self.input)
                    .hint_text("Enter a command")
                    .desired_width(400.0),
            );

            let send_clicked = ui.button("Send").clicked();

            let enter_pressed = input_response.lost_focus()
                && ui.input(|input| input.key_pressed(egui::Key::Enter));

            should_send = send_clicked || enter_pressed;

            if should_send {
                input_response.request_focus();
            }
        });

        should_send
    }

    fn send_message(&mut self) {
        let input = std::mem::take(&mut self.input);
        let input = input.trim();

        if input.is_empty() {
            return;
        }

        let user_message = input.to_owned();
        self.messages.push(Message {
            sender: Sender::User,
            text: user_message.clone(),
        });

        let reply = self.chatbot.respond(&user_message);
        self.messages.push(Message {
            sender: Sender::Hermes,
            text: reply.text,
        });
    }
}

#[derive(Clone, Copy)]
enum Sender {
    User,
    Hermes,
}

struct Message {
    sender: Sender,
    text: String,
}

impl eframe::App for GuiApp {
    fn ui(&mut self, ui: &mut egui::Ui, _frame: &mut eframe::Frame) {
        egui::CentralPanel::default().show(ui, |ui| {
            ui.heading("Hermes Mini");
            ui.separator();

            self.show_messages(ui);

            ui.separator();

            if self.show_input(ui) {
                self.send_message();
            }
        });
    }
}
