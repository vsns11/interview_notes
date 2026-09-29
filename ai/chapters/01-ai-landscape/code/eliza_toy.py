"""A 15-line ELIZA-style chatbot: pattern matching that *looks* like understanding."""
import re

RULES = [
    (r"i feel (.*)", "Why do you feel {0}?"),
    (r"i am (.*)", "How long have you been {0}?"),
    (r".*\bmother\b.*|.*\bfather\b.*", "Tell me more about your family."),
    (r"(.*)\?", "Why do you ask that?"),
    (r".*", "Please, go on."),
]
SWAP = {"my": "your", "me": "you", "i": "you", "am": "are"}


def reflect(fragment: str) -> str:
    return " ".join(SWAP.get(w, w) for w in fragment.split())


def reply(user: str) -> str:
    text = user.lower().strip(" .!")
    for pattern, template in RULES:
        m = re.fullmatch(pattern, text)
        if m:
            return template.format(*(reflect(g) for g in m.groups() if g is not None))
    return "Please, go on."


def main() -> None:
    for line in ["I feel stuck in my job.", "I am worried about exams",
                 "My mother calls every day", "Can you actually understand me?",
                 "The weather is nice"]:
        print(f"USER : {line}\nELIZA: {reply(line)}")


if __name__ == "__main__":
    main()
