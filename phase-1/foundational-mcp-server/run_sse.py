from main import mcp
import uvicorn

if __name__ == "__main__":
    # Get the SSE app instance from the method
    app = mcp.sse_app()
    uvicorn.run(app, host="0.0.0.0", port=8000)
