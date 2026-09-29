"""A toy, in-process imitation of MCP's JSON-RPC 2.0 messages.

NOT the real SDK - it shows the message shapes a host's MCP client and an MCP
server exchange (tools/list, tools/call, resources/read, prompts/get), and how
a host turns MCP tools into tool definitions for its LLM. Real servers use the
official SDK and a real transport (stdio or Streamable HTTP).
"""
import json

NOTES = {"notes://meeting/2026-09-21": "Decided: ship v2 on Oct 5. Owner: Priya."}


class ToyMCPServer:
    def __init__(self):
        self.handlers = {
            "tools/list": self.tools_list, "tools/call": self.tools_call,
            "resources/list": self.resources_list, "resources/read": self.resources_read,
            "prompts/list": self.prompts_list, "prompts/get": self.prompts_get,
        }

    # -- JSON-RPC plumbing: every request has jsonrpc, id, method, params
    def handle(self, raw: str) -> str:
        req = json.loads(raw)
        fn = self.handlers.get(req["method"])
        if fn is None:
            return json.dumps({"jsonrpc": "2.0", "id": req["id"],
                               "error": {"code": -32601, "message": "Method not found"}})
        return json.dumps({"jsonrpc": "2.0", "id": req["id"],
                           "result": fn(req.get("params", {}))})

    # -- tools: functions the MODEL may call (host asks the user first)
    def tools_list(self, _):
        return {"tools": [{"name": "search_notes",
                           "description": "Search meeting notes by keyword.",
                           "inputSchema": {"type": "object",
                                           "properties": {"query": {"type": "string"}},
                                           "required": ["query"]}}]}

    def tools_call(self, p):
        q = p["arguments"]["query"].lower()
        hits = [f"{uri}: {text}" for uri, text in NOTES.items() if q in text.lower()]
        return {"content": [{"type": "text", "text": "\n".join(hits) or "no matches"}],
                "isError": False}

    # -- resources: data the APPLICATION reads and may add to context
    def resources_list(self, _):
        return {"resources": [{"uri": u, "name": u.rsplit("/", 1)[-1],
                               "mimeType": "text/plain"} for u in NOTES]}

    def resources_read(self, p):
        return {"contents": [{"uri": p["uri"], "mimeType": "text/plain",
                              "text": NOTES[p["uri"]]}]}

    # -- prompts: reusable templates the USER picks (e.g. a slash command)
    def prompts_list(self, _):
        return {"prompts": [{"name": "summarize_meeting",
                             "arguments": [{"name": "uri", "required": True}]}]}

    def prompts_get(self, p):
        text = f"Summarize this meeting in 3 bullets:\n{NOTES[p['arguments']['uri']]}"
        return {"messages": [{"role": "user", "content": {"type": "text", "text": text}}]}


class ToyMCPClient:
    """Lives inside the host (chat app, IDE); one client per server connection."""

    def __init__(self, server):
        self.server, self.next_id = server, 0

    def request(self, method, params=None):
        self.next_id += 1
        msg = json.dumps({"jsonrpc": "2.0", "id": self.next_id, "method": method,
                          "params": params or {}})
        print("  ->", msg)
        reply = json.loads(self.server.handle(msg))
        print("  <-", json.dumps(reply)[:110] + ("..." if len(json.dumps(reply)) > 110 else ""))
        return reply.get("result", reply.get("error"))


def mcp_tools_to_llm_tools(mcp_tools):
    """Host glue: MCP 'inputSchema' -> the LLM API's tool format."""
    return [{"name": t["name"], "description": t["description"],
             "input_schema": t["inputSchema"]} for t in mcp_tools]


if __name__ == "__main__":
    client = ToyMCPClient(ToyMCPServer())
    print("1) discover tools")
    tools = client.request("tools/list")["tools"]
    print("   LLM tool definitions:", mcp_tools_to_llm_tools(tools))
    print("2) the model asked to call search_notes -> host calls the server")
    client.request("tools/call", {"name": "search_notes", "arguments": {"query": "ship"}})
    print("3) read a resource")
    client.request("resources/read", {"uri": "notes://meeting/2026-09-21"})
    print("4) fetch a prompt template")
    client.request("prompts/get", {"name": "summarize_meeting",
                                   "arguments": {"uri": "notes://meeting/2026-09-21"}})
    print("5) unknown method")
    client.request("files/delete")
