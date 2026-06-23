using Humanizer;
using System.Globalization;

var builder = WebApplication.CreateBuilder(args);

var app = builder.Build();

app.MapGet("/", (int n) =>
{
    return n.ToWords(
        new CultureInfo("es")
    );
});

app.Run();
