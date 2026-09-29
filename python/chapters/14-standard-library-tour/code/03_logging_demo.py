"""logging: levels, loggers, handlers, formatters, dictConfig, exceptions."""
import io
import logging
import logging.config

log = logging.getLogger("shop.orders")   # normally: logging.getLogger(__name__)


def place_order(qty: int) -> None:
    log.debug("validating qty=%s", qty)            # lazy %-style args
    if qty <= 0:
        log.warning("rejected order with qty=%d", qty)
        return
    try:
        1 / (qty - 3)
    except ZeroDivisionError:
        log.exception("pricing failed for qty=%d", qty)   # ERROR + traceback
        return
    log.info("order placed qty=%d", qty)


if __name__ == "__main__":
    file_like = io.StringIO()            # stands in for a log file
    logging.config.dictConfig({
        "version": 1,
        "disable_existing_loggers": False,
        "formatters": {
            "short": {"format": "%(levelname)-7s %(name)s: %(message)s"},
            "full": {"format": "%(asctime)s %(levelname)s %(name)s "
                               "%(filename)s:%(lineno)d %(message)s",
                     "datefmt": "%H:%M:%S"},
        },
        "handlers": {
            "console": {"class": "logging.StreamHandler", "level": "INFO",
                        "formatter": "short", "stream": "ext://sys.stdout"},
            "file": {"class": "logging.StreamHandler", "level": "DEBUG",
                     "formatter": "full", "stream": file_like},
        },
        "loggers": {"shop": {"level": "DEBUG", "handlers": ["console", "file"]}},
    })

    for q in (2, 0, 3):
        place_order(q)

    lines = file_like.getvalue().splitlines()
    print("--- file handler got", len(lines), "lines; DEBUG line present:",
          any("DEBUG" in ln for ln in lines))
    print("logger chain:", log.name, "->", log.parent.name, "->", log.parent.parent.name)
    print("effective level:", logging.getLevelName(log.getEffectiveLevel()))
