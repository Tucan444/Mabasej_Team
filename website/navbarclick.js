const object = { name: "John Smith" };
const json = JSON.stringify(object);  // {"name":"John Smith"}
console.log(json);
const unquoted = json.replace(/"([^"]+)":/g, '$1:');
console.log(unquoted);  // {name:"John Smith"}